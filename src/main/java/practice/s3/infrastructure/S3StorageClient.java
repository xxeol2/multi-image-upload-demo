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
import practice.s3.application.StorageClient;
import practice.s3.exception.InternalServerException;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageClient implements StorageClient {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file) {
        String fileName = generateFileName();
        try {
            log.info("[S3 File Upload] 시작 :{}", fileName);
            s3Client.putObject(bucket, fileName, file.getInputStream(), getObjectMetadata(file));
            log.info("[S3 File Upload] 완료 :{}", fileName);
            return fileName;
        } catch (SdkClientException | IOException e) {
            log.error("[S3 File Upload 실패]", e);
            throw new InternalServerException("[S3 File Upload 실패]", e);
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
            log.info("[S3 File Delete] {}", fileName);
        } catch (SdkClientException e) {
            log.error("[S3 File Delete 실패]", e);
            throw new InternalServerException("[S3 File Delete 실패]", e);
        }
    }
}
