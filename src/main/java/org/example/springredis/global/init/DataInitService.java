package org.example.springredis.global.init;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.chat.entity.Channel;
import org.example.springredis.domain.ch4.chat.repository.ChannelRepository;
import org.example.springredis.domain.ch4.comment.entity.Comment;
import org.example.springredis.domain.ch4.comment.repository.CommentRepository;
import org.example.springredis.domain.ch4.location.entity.Place;
import org.example.springredis.domain.ch4.location.repository.PlaceRepository;
import org.example.springredis.domain.ch4.post.entity.Post;
import org.example.springredis.domain.ch4.post.entity.PostTag;
import org.example.springredis.domain.ch4.post.entity.Tag;
import org.example.springredis.domain.ch4.post.repository.PostRepository;
import org.example.springredis.domain.ch4.post.repository.PostTagRepository;
import org.example.springredis.domain.ch4.post.repository.TagRepository;
import org.example.springredis.domain.ch4.user.entity.User;
import org.example.springredis.domain.ch4.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MySQL 초기 데이터를 하나의 트랜잭션으로 삽입.
 * Redis 작업은 포함하지 않음 → 트랜잭션 커밋 후 DataInitializer 에서 별도 처리.
 */
@Service
@RequiredArgsConstructor
public class DataInitService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final CommentRepository commentRepository;
    private final ChannelRepository channelRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public InitResult initMysql() {
        // ── Users ────────────────────────────────────────────────
        User player101 = userRepository.save(User.builder().username("player101").build());
        User player143 = userRepository.save(User.builder().username("player143").build());
        User player24  = userRepository.save(User.builder().username("player24").build());
        User player234 = userRepository.save(User.builder().username("player234").build());
        User player286 = userRepository.save(User.builder().username("player286").build());
        List<User> users = List.of(player101, player143, player24, player234, player286);

        // ── Tags ─────────────────────────────────────────────────
        Tag 의류  = tagRepository.save(Tag.builder().name("의류").build());
        Tag 잡화  = tagRepository.save(Tag.builder().name("잡화").build());
        Tag 아우터 = tagRepository.save(Tag.builder().name("아우터").build());
        Tag 하의  = tagRepository.save(Tag.builder().name("하의").build());
        Tag 상의  = tagRepository.save(Tag.builder().name("상의").build());
        List<Tag> tags = List.of(의류, 잡화, 아우터, 하의, 상의);

        // ── Posts ────────────────────────────────────────────────
        Post p1 = postRepository.save(Post.builder()
                .title("겨울 기모후드 추천").content("이번 겨울 기모후드 베스트 5를 소개합니다.").author(player101).build());
        Post p2 = postRepository.save(Post.builder()
                .title("에나멜 반지갑 리뷰").content("에나멜 소재 반지갑 장단점 분석입니다.").author(player234).build());
        Post p3 = postRepository.save(Post.builder()
                .title("코듀로이 아우터 코디").content("코듀로이 소재 아우터 코디 모음입니다.").author(player24).build());
        List<Post> posts = List.of(p1, p2, p3);

        // ── PostTags ─────────────────────────────────────────────
        postTagRepository.save(PostTag.builder().post(p1).tag(의류).build());
        postTagRepository.save(PostTag.builder().post(p1).tag(상의).build());
        postTagRepository.save(PostTag.builder().post(p2).tag(잡화).build());
        postTagRepository.save(PostTag.builder().post(p3).tag(의류).build());
        postTagRepository.save(PostTag.builder().post(p3).tag(아우터).build());

        // ── Comments ─────────────────────────────────────────────
        Comment c1 = commentRepository.save(Comment.builder()
                .post(p1).author(player234).content("저도 기모 좋아해요!").build());
        Comment c2 = commentRepository.save(Comment.builder()
                .post(p1).author(player286).content("어디서 구매하셨나요?").build());
        Comment c3 = commentRepository.save(Comment.builder()
                .post(p2).author(player101).content("에나멜 퀄리티 좋더라고요").build());
        List<Comment> comments = List.of(c1, c2, c3);

        // ── Channels ─────────────────────────────────────────────
        Channel ch1 = channelRepository.save(Channel.builder().name("공지사항").build());
        Channel ch2 = channelRepository.save(Channel.builder().name("자유채팅").build());
        Channel ch3 = channelRepository.save(Channel.builder().name("거래소").build());
        List<Channel> channels = List.of(ch1, ch2, ch3);

        // ── Places ───────────────────────────────────────────────
        Place pl1 = placeRepository.save(Place.builder().name("서울역").longitude(126.9707).latitude(37.5546).build());
        Place pl2 = placeRepository.save(Place.builder().name("강남역").longitude(127.0276).latitude(37.4979).build());
        Place pl3 = placeRepository.save(Place.builder().name("홍대입구역").longitude(126.9236).latitude(37.5572).build());
        Place pl4 = placeRepository.save(Place.builder().name("을지로입구역").longitude(126.9810).latitude(37.5660).build());
        Place pl5 = placeRepository.save(Place.builder().name("잠실역").longitude(127.1001).latitude(37.5133).build());
        List<Place> places = List.of(pl1, pl2, pl3, pl4, pl5);

        return new InitResult(users, tags, posts, comments, channels, places);
    }

    record InitResult(
            List<User> users,
            List<Tag> tags,
            List<Post> posts,
            List<Comment> comments,
            List<Channel> channels,
            List<Place> places
    ) {}
}
