package practice.s3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.s3.domain.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
