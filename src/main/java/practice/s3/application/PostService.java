package practice.s3.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.s3.domain.Image;
import practice.s3.domain.Post;
import practice.s3.dto.PostCreateRequest;
import practice.s3.dto.PostCreateResponse;
import practice.s3.repository.ImageRepository;
import practice.s3.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final ImageRepository imageRepository;

    public PostCreateResponse createPost(PostCreateRequest postRequest, List<String> imageUrls) {
        Post post = postRepository.save(new Post(postRequest.title(), postRequest.content()));
        List<Image> images = imageUrls.stream()
            .map(url -> new Image(post, url))
            .toList();
        imageRepository.saveAll(images);
        return PostCreateResponse.of(post, images);
    }
}
