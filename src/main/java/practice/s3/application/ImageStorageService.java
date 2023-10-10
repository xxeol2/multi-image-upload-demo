package practice.s3.application;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.dto.ImageUploadResponse;
import practice.s3.exception.ImageStorageException;

@Service
@Slf4j
public class ImageStorageService {

    private static final int MAX_IMAGE_LENGTH = 10;
    private static final long MAX_IMAGE_SIZE_BYTES = 1024 * 1024; // 1MB

    private final ImageStorageClient imageStorageClient;

    @Value("${s3.base-url}")
    private String baseUrl;

    public ImageStorageService(ImageStorageClient imageStorageClient) {
        this.imageStorageClient = imageStorageClient;
    }

    public ImageUploadResponse uploadFiles(MultipartFile[] imageFiles) {
        validate(imageFiles);
        List<CompletableFuture<String>> futures = Arrays.stream(imageFiles)
            .map(file -> CompletableFuture.supplyAsync(() -> imageStorageClient.upload(file)))
            .toList();

        return extractResponse(futures);
    }

    private void validate(MultipartFile[] imageFiles) {
        if (imageFiles.length > MAX_IMAGE_LENGTH) {
            throw new ImageStorageException("[File Upload 실패] 파일 수가 너무 많습니다.");
        }
        for (MultipartFile file : imageFiles) {
            validateImageFile(file);
            validateImageSize(file);
        }
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageStorageException("[File Upload 실패] image 형식이 아닙니다.");
        }
    }

    private void validateImageSize(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ImageStorageException("[File Upload 실패] 파일 크기가 1MB를 초과합니다.");
        }
    }

    private ImageUploadResponse extractResponse(List<CompletableFuture<String>> futures) {
        waitForAllJobFinished(futures);
        try {
            List<String> fileNames = futures.stream()
                .map(CompletableFuture::join)
                .toList();
            return convertFileNamesToResponse(fileNames);
        } catch (CompletionException e) {
            deleteUploadedFiles(futures);
            if (e.getCause() instanceof ImageStorageException) {
                throw (ImageStorageException) e.getCause();
            }
            throw new RuntimeException(e.getCause());
        }
    }

    private void waitForAllJobFinished(List<CompletableFuture<String>> futures) {
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();
    }

    private void deleteUploadedFiles(List<CompletableFuture<String>> futures) {
        futures.stream()
            .filter(CompletableFuture::isDone)
            .filter(future -> !future.isCompletedExceptionally())
            .map(CompletableFuture::join)
            .parallel()
            .forEach(imageStorageClient::delete);
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
            .forEach(imageStorageClient::delete);
    }
}
