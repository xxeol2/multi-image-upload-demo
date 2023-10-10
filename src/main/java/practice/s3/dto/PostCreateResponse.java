package practice.s3.dto;

import java.util.List;
import practice.s3.domain.Image;
import practice.s3.domain.Post;

public record PostCreateResponse(
    Long id,
    String title,
    List<ImageCreateResponse> images
) {

    public static PostCreateResponse of(Post post, List<Image> images) {
        return new PostCreateResponse(
            post.getId(),
            post.getTitle(),
            images.stream()
                .map(ImageCreateResponse::from)
                .toList()
        );
    }

    private record ImageCreateResponse(
        Long id,
        String url
    ) {

        public static ImageCreateResponse from(Image image) {
            return new ImageCreateResponse(
                image.getId(),
                image.getUrl()
            );
        }
    }
}
