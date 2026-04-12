# Spring Redis 학습 프로젝트

Redis 학습용 Spring Boot 프로젝트입니다.

## 기술 스택

- Java 21
- Spring Boot 4.0.5
- Spring Data Redis 4.0.4
- Lettuce (기본 Redis 클라이언트)
- Lombok

## Redis 실행 (Docker)

### 기본 실행

```bash
docker run -d --name redis -p 6379:6379 redis
```

### 데이터 영속성 + 비밀번호

```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  -v redis-data:/data \
  redis redis-server --requirepass "yourpassword" --appendonly yes
```

### Docker Compose

```yaml
version: '3.8'

services:
  redis:
    image: redis:latest
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - ./redis.conf:/etc/redis/redis.conf
      - redis-data:/data
    command: redis-server /etc/redis/redis.conf
    ulimits:
      nofile:
        soft: 100000
        hard: 100000
    sysctls:
      - net.core.somaxconn=65535
      - net.ipv4.tcp_max_syn_backlog=65535
    restart: unless-stopped

volumes:
  redis-data:
```

```bash
docker compose up -d
```

### 연결 확인

```bash
# CLI 접속
docker exec -it redis redis-cli

# 비밀번호 있는 경우
docker exec -it redis redis-cli -a yourpassword

# ping 테스트
127.0.0.1:6379> PING
# PONG 응답 확인
```

### 자주 쓰는 Docker 명령어

| 명령어 | 설명 |
|--------|------|
| `docker stop redis` | 중지 |
| `docker start redis` | 시작 |
| `docker logs redis` | 로그 확인 |
| `docker rm -f redis` | 삭제 |

---

## Spring Boot 설정

### application.yml

```yaml
spring:
  application:
    name: spring-redis
  data:
    redis:
      host: 127.0.0.1
      port: 6379
```

비밀번호를 설정한 경우 `password` 항목 추가:

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: yourpassword
```

---

## 서버 환경 설정 (운영/Linux)

Docker 컨테이너 내부의 Redis는 **호스트 OS의 커널 파라미터**를 그대로 사용하므로, 아래 설정은 호스트 머신에서 적용해야 합니다.

### Open Files (파일 디스크립터)

Redis 기본 `maxclients = 10000`, 내부적으로 파일 디스크립터 32개를 추가 사용하므로 실제 `10032`개 필요.

`/etc/security/limits.conf`에 추가:

```
*     hard    nofile    100000
*     soft    nofile    100000
```

### THP (Transparent Huge Pages) 비활성화

THP는 Redis에서 오히려 리소스를 과다 소비하므로 비활성화 권장.

`/etc/rc.local`에 추가:

```bash
if test -f /sys/kernel/mm/transparent_hugepage/enabled; then
    echo never > /sys/kernel/mm/transparent_hugepage/enabled
fi
```

### vm.overcommit_memory

Redis는 디스크 저장 시 `fork()` + COW(Copy-On-Write) 방식으로 동작. overcommit 허용 필요.

`/etc/sysctl.conf`에 추가:

```
vm.overcommit_memory = 1
```

```bash
sysctl -w vm.overcommit_memory=1  # 즉시 적용
```

### somaxconn / syn_backlog

Redis의 `tcp-backlog` 값은 호스트의 `somaxconn`, `syn_backlog`를 초과할 수 없음.

`/etc/sysctl.conf`에 추가:

```
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65535
```

```bash
sysctl -p  # 즉시 적용
```

---

## redis.conf 주요 설정

```
# 포트 (기본값 6379)
port 6379

# Docker 환경에서는 0.0.0.0 (포트 접근 제어는 Docker -p 옵션으로 관리)
bind 0.0.0.0

# 패스워드 미설정 시 로컬에서만 접근
protected-mode yes

requirepass yourpassword
masterauth yourpassword   # 복제 구성 시 마스터 패스워드

# Docker 환경에서는 반드시 no (yes 설정 시 컨테이너 즉시 종료)
daemonize no

dir /data

maxclients 1000
tcp-backlog 511
```

---

## 프로젝트 구조

```
src/main/java/org/example/springredis/
├── config/
│   └── RedisConfig.java          # RedisTemplate 빈 설정
└── service/
    ├── StringService.java         # String - SET/GET/INCR/MSET 등
    ├── ListService.java           # List - LPUSH/RPUSH/LRANGE/LTRIM 등
    ├── HashService.java           # Hash - HSET/HGET/HGETALL 등
    ├── SetService.java            # Set - SADD/SMEMBERS/집합 연산 등
    ├── SortedSetService.java      # Sorted Set - ZADD/ZRANGE/BYLEX 등
    ├── BitmapService.java         # Bitmap - SETBIT/GETBIT/BITCOUNT 등
    ├── HyperLogLogService.java    # HyperLogLog - PFADD/PFCOUNT
    ├── GeoService.java            # Geospatial - GEOADD/GEOPOS/GEODIST
    ├── StreamService.java         # Stream - XADD/XREAD/XLEN
    └── KeyManagementService.java  # 키 관리 - SCAN/TTL/EXPIRE/DEL 등
```
