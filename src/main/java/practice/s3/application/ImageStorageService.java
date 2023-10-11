package practice.s3.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.dto.ImageUploadResponse;
import practice.s3.exception.BadRequestException;
import practice.s3.exception.InternalServerException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageStorageService {

    private static final int MAX_IMAGE_LENGTH = 10;

    private final StorageClient storageClient;
    private final Executor executor;

    @Value("${s3.base-url}")
    private String baseUrl;

    public ImageUploadResponse uploadFiles(MultipartFile[] imageFiles) {
        validate(imageFiles);
        List<CompletableFuture<String>> futures = Arrays.stream(imageFiles)
            .map(file -> CompletableFuture.supplyAsync(() -> storageClient.upload(file), executor))
            .toList();

        return extractResponse(futures);
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

    private ImageUploadResponse extractResponse(List<CompletableFuture<String>> futures) {
        List<String> fileNames = new ArrayList<>();
        AtomicBoolean catchException = new AtomicBoolean(false);
        futures.forEach(future -> {
            try {
                fileNames.add(future.join());
            } catch (CompletionException e) {
                catchException.set(true);
            }
        });
        handleException(catchException, fileNames);
        return convertFileNamesToResponse(fileNames);
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

    public void deleteFiles(List<String> fileNames) {
        fileNames.stream()
            .parallel()
            .forEach(storageClient::delete);
    }
}
