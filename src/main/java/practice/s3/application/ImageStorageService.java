package practice.s3.application;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.dto.ImageUploadResponse;
import practice.s3.exception.ImageStorageException;

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
        AtomicReference<ImageStorageException> exceptionHolder = new AtomicReference<>();
        List<String> fileNames = uploadFilesInParallel(imageFiles, exceptionHolder);

        if (exceptionHolder.get() != null) {
            executor.execute(() -> deleteUploadedFiles(fileNames));
            throw exceptionHolder.get();
        }

        return convertFileNamesToResponse(fileNames);
    }

    private void validate(MultipartFile[] imageFiles) {
        if (imageFiles.length > MAX_IMAGE_LENGTH) {
            throw new ImageStorageException("[File Upload 실패] 파일 수가 너무 많습니다.");
        }
        Arrays.stream(imageFiles)
            .forEach(this::validateImageFile);
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageStorageException("[File Upload 실패] image 형식이 아닙니다.");
        }
    }

    private List<String> uploadFilesInParallel(MultipartFile[] imageFiles,
                                               AtomicReference<ImageStorageException> exceptionHolder) {
        return Arrays.stream(imageFiles)
            .parallel()
            .map(file -> uploadSingleFile(file, exceptionHolder))
            .filter(Objects::nonNull)
            .toList();
    }

    private String uploadSingleFile(MultipartFile file, AtomicReference<ImageStorageException> exceptionHolder) {
        if (exceptionHolder.get() != null) {
            return null;
        }
        try {
            return imageStorageClient.upload(file);
        } catch (ImageStorageException e) {
            exceptionHolder.set(e);
            return null;
        }
    }

    private void deleteUploadedFiles(List<String> fileNames) {
        fileNames.stream()
            .parallel()
            .forEach(imageStorageClient::delete);
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
