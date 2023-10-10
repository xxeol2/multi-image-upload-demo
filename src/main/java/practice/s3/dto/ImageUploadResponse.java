package practice.s3.dto;

import java.util.List;

public record ImageUploadResponse(
    List<String> urls,
    List<String> fileNames
) {

}
