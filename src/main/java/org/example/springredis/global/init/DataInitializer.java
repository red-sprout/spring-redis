package org.example.springredis.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springredis.domain.ch4.chat.entity.Channel;
import org.example.springredis.domain.ch4.chat.service.ChatService;
import org.example.springredis.domain.ch4.comment.entity.Comment;
import org.example.springredis.domain.ch4.comment.service.CommentService;
import org.example.springredis.domain.ch4.analytics.service.AnalyticsService;
import org.example.springredis.domain.ch4.leaderboard.service.LeaderboardService;
import org.example.springredis.domain.ch4.location.service.LocationService;
import org.example.springredis.domain.ch4.post.entity.Post;
import org.example.springredis.domain.ch4.post.entity.Tag;
import org.example.springredis.domain.ch4.post.service.PostService;
import org.example.springredis.domain.ch4.search.service.SearchService;
import org.example.springredis.domain.ch4.user.entity.User;
import org.example.springredis.domain.ch4.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final DataInitService dataInitService;
    private final LeaderboardService leaderboardService;
    private final SearchService searchService;
    private final PostService postService;
    private final CommentService commentService;
    private final ChatService chatService;
    private final AnalyticsService analyticsService;
    private final LocationService locationService;

    private static final String DATE = "260413";

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("[DataInitializer] 이미 초기화된 데이터가 있습니다. 스킵합니다.");
            return;
        }

        log.info("[DataInitializer] 샘플 데이터 초기화 시작");

        // ── MySQL (단일 트랜잭션) ──────────────────────────────────────
        DataInitService.InitResult result = dataInitService.initMysql();

        User player101 = result.users().get(0);
        User player143 = result.users().get(1);
        User player24  = result.users().get(2);
        User player234 = result.users().get(3);
        User player286 = result.users().get(4);

        Tag 의류  = result.tags().get(0);
        Tag 잡화  = result.tags().get(1);
        Tag 아우터 = result.tags().get(2);
        Tag 하의  = result.tags().get(3);
        Tag 상의  = result.tags().get(4);

        Post p1 = result.posts().get(0);
        Post p2 = result.posts().get(1);
        Post p3 = result.posts().get(2);

        Comment c1 = result.comments().get(0);
        Comment c2 = result.comments().get(1);
        Comment c3 = result.comments().get(2);

        Channel ch1 = result.channels().get(0);
        Channel ch2 = result.channels().get(1);
        Channel ch3 = result.channels().get(2);

        log.info("[DataInitializer] MySQL 초기화 완료 (단일 트랜잭션)");

        // ── Redis (MySQL 커밋 이후 처리) ──────────────────────────────

        // Leaderboard (Sorted Set)
        leaderboardService.addScore(DATE, player286.getId(), 28);
        leaderboardService.addScore(DATE, player234.getId(), 400);
        leaderboardService.addScore(DATE, player101.getId(), 45);
        leaderboardService.addScore(DATE, player24.getId(),  357);
        leaderboardService.addScore(DATE, player143.getId(), 199);
        leaderboardService.addScore("260406", player101.getId(), 50);
        leaderboardService.addScore("260406", player24.getId(),  250);
        leaderboardService.addScore("260407", player286.getId(), 200);
        leaderboardService.addScore("260407", player24.getId(),  350);
        leaderboardService.addScore("260407", player234.getId(), 400);
        leaderboardService.addScore("260408", player24.getId(),  50);
        leaderboardService.addScore("260408", player101.getId(), 100);
        leaderboardService.addScore("260408", player234.getId(), 250);
        log.info("[DataInitializer] Leaderboard 초기화 완료");

        // Search history (Sorted Set)
        searchService.addKeyword(player101.getId(), "실버");
        searchService.addKeyword(player101.getId(), "에나멜");
        searchService.addKeyword(player101.getId(), "반지갑");
        searchService.addKeyword(player101.getId(), "코듀로이");
        searchService.addKeyword(player101.getId(), "기모후드");
        log.info("[DataInitializer] Search history 초기화 완료");

        // Tag-to-post Redis cache (Set) — MySQL PostTag는 이미 커밋됨
        postService.cacheTagPost(의류.getId(),  p1.getId());
        postService.cacheTagPost(상의.getId(),  p1.getId());
        postService.cacheTagPost(잡화.getId(),  p2.getId());
        postService.cacheTagPost(의류.getId(),  p3.getId());
        postService.cacheTagPost(아우터.getId(), p3.getId());
        log.info("[DataInitializer] Tag 캐시 초기화 완료");

        // Comment likes (Set)
        commentService.addLike(c1.getId(), player143.getId());
        commentService.addLike(c1.getId(), player24.getId());
        commentService.addLike(c1.getId(), player286.getId());
        commentService.addLike(c2.getId(), player101.getId());
        commentService.addLike(c3.getId(), player234.getId());
        commentService.addLike(c3.getId(), player286.getId());
        log.info("[DataInitializer] Comment 좋아요 초기화 완료");

        // Chat unread counts (Hash)
        chatService.joinChannel(player101.getId(), ch1.getId());
        chatService.joinChannel(player101.getId(), ch2.getId());
        chatService.joinChannel(player101.getId(), ch3.getId());
        chatService.joinChannel(player234.getId(), ch2.getId());
        chatService.joinChannel(player234.getId(), ch3.getId());
        for (int i = 0; i < 3;   i++) chatService.receiveMessage(player101.getId(), ch1.getId());
        for (int i = 0; i < 27;  i++) chatService.receiveMessage(player101.getId(), ch2.getId());
        for (int i = 0; i < 128; i++) chatService.receiveMessage(player101.getId(), ch3.getId());
        for (int i = 0; i < 5;   i++) chatService.receiveMessage(player234.getId(), ch2.getId());
        chatService.receiveMessage(player234.getId(), ch3.getId());
        log.info("[DataInitializer] Chat 초기화 완료");

        // Analytics (Bitmap + HyperLogLog)
        analyticsService.recordVisit(DATE, player101.getId());
        analyticsService.recordVisit(DATE, player24.getId());
        analyticsService.recordVisit(DATE, player234.getId());
        for (int i = 1; i <= 10; i++) analyticsService.recordApiCall(player101.getId(), "log-" + i);
        for (int i = 1; i <= 5;  i++) analyticsService.recordApiCall(player234.getId(), "log-" + i);
        log.info("[DataInitializer] Analytics 초기화 완료");

        // Geo locations (Redis) — MySQL은 이미 커밋됨
        locationService.syncAllToRedis();
        log.info("[DataInitializer] Locations 초기화 완료");

        log.info("[DataInitializer] 샘플 데이터 초기화 완료");
    }
}
