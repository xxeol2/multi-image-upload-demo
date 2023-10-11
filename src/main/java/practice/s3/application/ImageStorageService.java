package practice.s3.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.exception.ImageStorageException;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private static final int MAX_IMAGE_LENGTH = 10;

    private final ImageStorageClient imageStorageClient;

    @Value("${s3.base-url}")
    private String baseUrl;

    public List<String> uploadFiles(MultipartFile[] imageFiles) {
        validate(imageFiles);
        List<String> fileNames = new ArrayList<>();
        try {
            Arrays.stream(imageFiles)
                .map(imageStorageClient::upload)
                .forEach(fileNames::add);
            return convertToUrl(fileNames);
        } catch (ImageStorageException e) {
            fileNames.forEach(imageStorageClient::delete);
            throw e;
        }
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

    private List<String> convertToUrl(List<String> fileNames) {
        return fileNames.stream()
            .map(this::convertFileNameToUrl)
            .toList();
    }

    private String convertFileNameToUrl(String fileName) {
        return baseUrl + fileName;
    }
}
