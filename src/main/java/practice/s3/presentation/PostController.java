package practice.s3.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import practice.s3.application.PostFacadeService;
import practice.s3.dto.PostCreateRequest;
import practice.s3.dto.PostCreateResponse;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostFacadeService postFacadeService;

    @PostMapping
    public ResponseEntity<PostCreateResponse> create(
        @RequestPart("post") PostCreateRequest postRequest,
        @RequestPart(value = "images", required = false) MultipartFile[] imageFiles
    ) {
        if (imageFiles == null) {
            imageFiles = new MultipartFile[0];
        }
        PostCreateResponse response = postFacadeService.createPost(postRequest, imageFiles);
        return ResponseEntity.ok(response);
    }
}
