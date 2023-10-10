package practice.s3.application;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.exception.ImageStorageException;

@Service
@Slf4j
public class ImageStorageService {

    private final ImageStorageClient imageStorageClient;

    @Value("${s3.base-url}")
    private String baseUrl;

    public ImageStorageService(ImageStorageClient imageStorageClient) {
        this.imageStorageClient = imageStorageClient;
    }

    public List<String> uploadFiles(MultipartFile[] imageFiles) {
        List<CompletableFuture<String>> futures = Arrays.stream(imageFiles)
            .map(file -> CompletableFuture.supplyAsync(() -> imageStorageClient.upload(file)))
            .toList();

        return extractUploadedFileUrls(futures);
    }

    private List<String> extractUploadedFileUrls(List<CompletableFuture<String>> futures) {
        waitForAllJobFinished(futures);
        try {
            return futures.stream()
                .map(CompletableFuture::join)
                .map(this::convertFileNameToUrl)
                .toList();
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

    private String convertFileNameToUrl(String fileName) {
        return baseUrl + fileName;
    }
}
