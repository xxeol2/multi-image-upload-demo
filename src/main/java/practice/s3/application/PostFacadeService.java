package practice.s3.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.aop.Timer;
import practice.s3.dto.ImageUploadResponse;
import practice.s3.dto.PostCreateRequest;
import practice.s3.dto.PostCreateResponse;

@Service
@RequiredArgsConstructor
public class PostFacadeService {

    private final PostService postService;
    private final ImageStorageService imageStorageService;

    @Timer
    public PostCreateResponse createPost(PostCreateRequest postRequest, MultipartFile[] imageFiles) {
        ImageUploadResponse response = imageStorageService.uploadFiles(imageFiles);
        try {
            return postService.createPost(postRequest, response.urls());
        } catch (Exception e) {
            imageStorageService.deleteFiles(response.fileNames());
            throw e;
        }
    }
}
