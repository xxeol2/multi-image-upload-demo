package practice.s3.infrastructure;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.application.ImageStorageClient;
import practice.s3.exception.ImageStorageException;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ImageStorageClient implements ImageStorageClient {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file) {
        validateImageFile(file);
        String fileName = generateFileName();
        try {
            log.info("[S3 File Upload] 시작 :{}", fileName);
            s3Client.putObject(bucket, fileName, file.getInputStream(), getObjectMetadata(file));
            log.info("[S3 File Upload] 완료 :{}", fileName);
            return fileName;
        } catch (SdkClientException | IOException e) {
            throw new ImageStorageException("[S3 File Upload 실패]", e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageStorageException("[S3 File Upload 실패] image 형식이 아닙니다.");
        }
    }

    private String generateFileName() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss").format(LocalDateTime.now()) + "-" + UUID.randomUUID();
    }

    private ObjectMetadata getObjectMetadata(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        return objectMetadata;
    }

    @Override
    public void delete(String fileName) {
        try {
            s3Client.deleteObject(bucket, fileName);
        } catch (SdkClientException e) {
            throw new ImageStorageException("[S3 File Delete 실패]", e);
        }
    }
}
