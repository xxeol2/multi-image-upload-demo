package practice.s3.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.dto.ImageUploadResponse;
import practice.s3.exception.BadRequestException;
import practice.s3.exception.InternalServerException;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private static final int MAX_IMAGE_LENGTH = 10;

    private final ImageStorageClient imageStorageClient;
    private final Executor executor;

    @Value("${s3.base-url}")
    private String baseUrl;

    public ImageUploadResponse uploadFiles(MultipartFile[] imageFiles) {
        validate(imageFiles);
        List<String> fileNames = new ArrayList<>();
        try {
            Arrays.stream(imageFiles)
                .map(imageStorageClient::upload)
                .forEach(fileNames::add);
            return convertFileNamesToResponse(fileNames);
        } catch (BadRequestException e) {
            executor.execute(() -> deleteFiles(fileNames));
            throw new InternalServerException("이미지 업로드시 예외가 발생했습니다.");
        }
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

    private ImageUploadResponse convertFileNamesToResponse(List<String> fileNames) {
        List<String> urls = fileNames.stream()
            .map(fileName -> baseUrl + fileName)
            .toList();
        return new ImageUploadResponse(urls, fileNames);
    }

    @Async
    public void deleteFiles(List<String> fileNames) {
        fileNames.forEach(imageStorageClient::delete);
    }
}
