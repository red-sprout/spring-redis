package org.example.springredis.domain.ch4.post.repository;

import org.example.springredis.domain.ch4.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
