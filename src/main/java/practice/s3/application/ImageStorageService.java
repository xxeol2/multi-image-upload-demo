package practice.s3.application;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.dto.ImageUploadResponse;
import practice.s3.exception.BadRequestException;
import practice.s3.exception.InternalServerException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageService {

    private static final int MAX_IMAGE_LENGTH = 10;

    private final ImageStorageClient imageStorageClient;
    private final Executor executor;

    @Value("${s3.base-url}")
    private String baseUrl;

    public ImageUploadResponse uploadFiles(MultipartFile[] imageFiles) {
        validate(imageFiles);
        AtomicBoolean catchException = new AtomicBoolean(false);
        List<String> fileNames = uploadFilesInParallel(imageFiles, catchException);
        handleException(catchException, fileNames);
        return convertFileNamesToResponse(fileNames);
    }

    private void validate(MultipartFile[] imageFiles) {
        if (imageFiles.length > MAX_IMAGE_LENGTH) {
            throw new BadRequestException("파일 수가 너무 많습니다.");
        }
        Arrays.stream(imageFiles)
            .forEach(this::validateImageFile);
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("image 형식이 아닙니다.");
        }
    }

    private List<String> uploadFilesInParallel(MultipartFile[] imageFiles, AtomicBoolean catchException) {
        return Arrays.stream(imageFiles)
            .parallel()
            .map(file -> uploadSingleFile(file, catchException))
            .filter(Objects::nonNull)
            .toList();
    }

    private String uploadSingleFile(MultipartFile file, AtomicBoolean exceptionHolder) {
        if (exceptionHolder.get()) {
            return null;
        }
        try {
            return imageStorageClient.upload(file);
        } catch (Exception e) {
            exceptionHolder.set(true);
            return null;
        }
    }

    private void handleException(AtomicBoolean catchException, List<String> fileNames) {
        if (catchException.get()) {
            executor.execute(() -> deleteFiles(fileNames));
            throw new InternalServerException("이미지 업로드시 예외가 발생했습니다.");
        }
    }

    private ImageUploadResponse convertFileNamesToResponse(List<String> fileNames) {
        List<String> urls = fileNames.stream()
            .map(fileName -> baseUrl + fileName)
            .toList();
        return new ImageUploadResponse(urls, fileNames);
    }

    @Async
    public void deleteFiles(List<String> fileNames) {
        fileNames
            .parallelStream()
            .forEach(imageStorageClient::delete);
    }
}
