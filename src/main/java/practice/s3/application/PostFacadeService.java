package practice.s3.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.aop.Timer;
import practice.s3.dto.PostCreateRequest;
import practice.s3.dto.PostCreateResponse;

@Service
@RequiredArgsConstructor
public class PostFacadeService {

    private final PostService postService;
    private final ImageStorageService imageStorageService;

    @Timer
    public PostCreateResponse createPost(PostCreateRequest postRequest, MultipartFile[] imageRequests) {
        List<String> imageUrls = imageStorageService.uploadFiles(imageRequests);
        return postService.createPost(postRequest, imageUrls);
    }
}
