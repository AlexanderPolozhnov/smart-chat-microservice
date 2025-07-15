# Smart Chat Microservice

������������������� ����������� ��� ������� � �������� �������, ������������� �� Spring Boot � �������������� Kafka,
Redis � PostgreSQL.

## ����������� �������

1. **�������������� � ����������� (Spring Security + JWT)**
    - ��������� Access � Refresh �������
    - ����������� ������� � BCrypt
    - ������ ������ (blacklist) JWT � Redis ��� logout

2. **�������������� �����������**
    - Producer -> Kafka -> Consumer
    - ����������� ��������� ��������� � ThreadPool

3. **�������� ������**
    - **PostgreSQL** ��� ��������������� �����, ������������� � ������� ���������
    - **Redis** ��� ����������� ��������� N ��������� � �������� �������

4. **REST API**
    - CRUD ��� ����� � �������������
    - �������� ��������� Kafka Producer
    - ������ �������, ����� �� ������, ���������� �� �����

5. **������������ � ����������**
    - **Swagger UI**: ������������� ������������ OpenAPI
    - **Actuator**: health, metrics, httpexchanges, info

6. **��������������**
    - **Docker Compose**: PostgreSQL, Redis, Zookeeper, Kafka, ����������

## ������� �����

```bash
# ������������ �����������
git clone https://github.com/AlexanderPolozhnov/smart-chat-microservice.git
cd smart-chat-microservice

# ������ ��� ������
mvn clean package -DskipTests

# ������ ��������������
docker-compose up -d

# ������ ����������
docker-compose exec app java -jar /app/app.jar
```

����� ������� ��������:

* Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* Actuator health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## ��������� �������

```
src/
   main/
      java/com/alexander/smartchat
         config/              # Kafka, Swagger, Security, Actuator
         controller/          # REST endpoints (@Tag annotations)
         dto/                 # Data Transfer Objects
         entity/              # JPA Entities
         exception/           # Custom and global exception
         filter/              # JwtFilter
         mapper/              # MapStruct interfaces
         repository/          # Spring Data JPA
         service/             # Business logic
            jwt/              # JwtAuthEntryPoint, JwtProvider
            kafka/            # Producer & Consumer
            redis/            # RedisCacheService
         util/                # JwtParseUtil, others
   resources/
      db/changelog/
      application.yml
      application-local.yml (.gitignore)
      application-docker.yml
   test/                      # Unit & Integration tests
docker-compose.yml
Dockerfile
README.md
```

## ������������

### application.yml

```yaml
spring:
  profiles:
    default: local

  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/smart_chat_db
    username: postgres
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
      jdbc:
        batch_size: 50

  data:
    redis:
      host: localhost
      port: 6379

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yml
    enabled: true

  security:
    user:
      name: none

  graphql:
    cors:
      allowed-origins: "*"

  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: smartchat-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.alexander.smartchat.dto

kafka:
  topic:
    name: chat-messages

chat:
  message-cache-ttl: PT24H

jwt:
  secret:
    access: <base64-access-secret>
    refresh: <base64-refresh-secret>
  access-validity-minutes: 15
  refresh-validity-days: 5

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,httpexchanges
  endpoint:
    health:
      show-details: always
```

## ����� ��������

| ������        | ���� |
|---------------|------|
| PostgreSQL    | 5432 |
| Redis         | 6379 |
| Zookeeper     | 2181 |
| Kafka Broker  | 9092 |
| SmartChat App | 8080 |

## ������������

```bash
# Unit + WebMvc �����
mvn test

# ����� �� ��������
mvn jacoco:report
```

## ������������

![Swagger UI](docs/swagger-screenshot.png)

1. **�����������**: POST /auth/signup
2. **�����**:      POST /auth/login
3. **������� ���**: POST /api/chats
4. **��������� ���������**: POST /api/messages
5. **�������**:    GET /api/messages
