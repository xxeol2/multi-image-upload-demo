package practice.s3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.s3.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
