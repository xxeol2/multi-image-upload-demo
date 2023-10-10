package practice.s3.application;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

//@Component
@Slf4j
public class MockImageStorageClient implements ImageStorageClient {

    @Override
    public String upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        log.info("[Upload start] {}", fileName);
        try {
            Thread.sleep(300);
            String generatedFileName = UUID.randomUUID() + "-" + fileName;
            log.info("[Upload finished] {} -> {}", file, generatedFileName);
            return generatedFileName;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String fileName) {
        log.info("[Delete start] {}", fileName);
        try {
            Thread.sleep(300);
            log.info("[Delete finished] {}", fileName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
