# Spring Redis 학습 프로젝트

Redis + MySQL 을 함께 활용하는 Spring Boot 학습 프로젝트입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Redis | Spring Data Redis 4.x, Lettuce |
| MySQL | Spring Data JPA, Hibernate 7.x |
| Docs | SpringDoc OpenAPI (Swagger) |
| Etc | Lombok |

---

## 프로젝트 구조

```
src/main/java/org/example/springredis/
├── global/
│   └── config/
│       └── RedisConfig.java          # RedisTemplate 빈 설정 (전역)
└── domain/
    ├── ch3/                          # 3장: Redis 자료구조 학습
    │   ├── model/Product.java
    │   ├── repository/ProductRepository.java
    │   └── service/
    │       ├── StringService.java        # String - SET/GET/INCR/MSET 등
    │       ├── ListService.java          # List - LPUSH/RPUSH/LRANGE 등
    │       ├── HashService.java          # Hash - HSET/HGET/HGETALL 등
    │       ├── SetService.java           # Set - SADD/SMEMBERS/집합 연산
    │       ├── SortedSetService.java     # Sorted Set - ZADD/ZRANGE 등
    │       ├── BitmapService.java        # Bitmap - SETBIT/GETBIT/BITCOUNT
    │       ├── HyperLogLogService.java   # HyperLogLog - PFADD/PFCOUNT
    │       ├── GeoService.java           # Geo - GEOADD/GEOPOS/GEODIST
    │       ├── StreamService.java        # Stream - XADD/XREAD/XLEN
    │       ├── KeyManagementService.java # 키 관리 - SCAN/TTL/DEL 등
    │       └── ProductService.java       # @RedisHash + RedisTemplate 비교
    └── ch4/                          # 4장: Redis 자료구조 활용 사례 (Redis + MySQL)
        ├── user/                         # 회원 (MySQL)
        ├── leaderboard/                  # 실시간 리더보드 (Sorted Set)
        ├── search/                       # 최근 검색 기록 (Sorted Set)
        ├── post/                         # 게시글 + 태그 (MySQL + Set 캐시)
        ├── comment/                      # 댓글 + 좋아요 (MySQL + Set)
        ├── chat/                         # 읽지 않은 메시지 (MySQL + Hash)
        ├── analytics/                    # DAU (Bitmap) + 미터링 (HyperLogLog)
        └── location/                     # 위치 기반 검색 (MySQL + Geo Set)
```

---

## 시작하기

### 1. Redis 실행 (Docker)

```bash
docker run -d --name redis -p 6379:6379 redis
```

연결 확인:
```bash
docker exec -it redis redis-cli ping
# PONG
```

### 2. MySQL 설정

```sql
-- DB 생성
CREATE DATABASE spring_redis;

-- 사용자 생성 (예시)
SET GLOBAL validate_password.policy = LOW;
SET GLOBAL validate_password.length = 4;
CREATE USER 'redis'@'localhost' IDENTIFIED BY 'redis';
GRANT ALL PRIVILEGES ON *.* TO 'redis'@'localhost' WITH GRANT OPTION;
```

### 3. secret.yml 생성

`src/main/resources/secret.yml` 파일을 생성하세요. (git에서 제외됨)

```yaml
# secret.yml.example 참고
spring:
  datasource:
    username: your_mysql_username
    password: your_mysql_password
```

### 4. 실행

```bash
./gradlew bootRun
```

---

## API 문서 (Swagger)

앱 실행 후 아래 URL에서 전체 API를 확인하고 테스트할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

### 주요 엔드포인트

| 도메인 | Base URL | 설명 |
|--------|----------|------|
| User | `/api/ch4/users` | 회원 CRUD |
| Leaderboard | `/api/ch4/leaderboard` | 일간/주간 리더보드 |
| Search | `/api/ch4/search` | 최근 검색 기록 |
| Post | `/api/ch4/posts` | 게시글 + 태그 |
| Comment | `/api/ch4/comments` | 댓글 + 좋아요 |
| Chat | `/api/ch4/chat` | 읽지 않은 메시지 |
| Analytics | `/api/ch4/analytics` | DAU, API 미터링 |
| Location | `/api/ch4/places` | 위치 기반 검색 |

---

## Redis + MySQL 활용 패턴 (4장)

| 기능 | MySQL 역할 | Redis 역할 |
|------|-----------|-----------|
| 리더보드 | - | Sorted Set (점수 정렬) |
| 최근 검색 | - | Sorted Set (시간순, 최대 5개) |
| 태그 교집합 | 태그/게시글 영속 저장 | Set (교집합 조회 캐시) |
| 좋아요 | 댓글 영속 저장 | Set (중복 방지 + 카운팅) |
| 읽지 않은 메시지 | 채널 영속 저장 | Hash (유저별 채널 카운트) |
| DAU | - | Bitmap (유저 방문 여부) |
| API 미터링 | - | HyperLogLog (유니크 카운팅) |
| 위치 검색 | 장소 영속 저장 | Geo Set (반경 검색) |

---

## Docker Compose (Redis)

```yaml
version: '3.8'

services:
  redis:
    image: redis:latest
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    restart: unless-stopped

volumes:
  redis-data:
```

```bash
docker compose up -d
```
