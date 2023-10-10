package practice.s3.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        List<String> fileNames = Collections.synchronizedList(new ArrayList<>());
        try {
            Arrays.stream(imageFiles)
                .parallel()
                .map(imageStorageClient::upload)
                .forEach(fileNames::add);
            return convertToUrl(fileNames);
        } catch (ImageStorageException e) {
            log.info("[Catch Exception] uploadedFileSize: {}", fileNames.size());
            fileNames.forEach(imageStorageClient::delete);
            throw e;
        }
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
