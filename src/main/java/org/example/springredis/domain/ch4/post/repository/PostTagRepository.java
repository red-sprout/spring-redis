package org.example.springredis.domain.ch4.post.repository;

import org.example.springredis.domain.ch4.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByTagId(Long tagId);
    List<PostTag> findByPostId(Long postId);
}
