package practice.s3.application;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
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
        AtomicReference<ImageStorageException> exceptionHolder = new AtomicReference<>();
        List<String> fileNames = uploadFilesInParallel(imageFiles, exceptionHolder);

        if (exceptionHolder.get() != null) {
            deleteUploadedFiles(fileNames);
            throw exceptionHolder.get();
        }

        return convertToUrl(fileNames);
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

    private List<String> convertToUrl(List<String> fileNames) {
        return fileNames.stream()
            .map(this::convertFileNameToUrl)
            .toList();
    }

    private String convertFileNameToUrl(String fileName) {
        return baseUrl + fileName;
    }
}
