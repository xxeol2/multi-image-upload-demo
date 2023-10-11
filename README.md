# 다중 이미지 업로드 최적화 :병렬 스트림과 CompletableFuture

이 저장소는 `다중 이미지 업로드 최적화 :병렬 스트림과 CompletableFuture` 포스트의 예제 코드를 담고 있습니다.

## 브랜치별 예제 바로가기
[순차적 업로드](https://github.com/xxeol2/s3-practice/tree/sequential)

[병렬 스트림](https://github.com/xxeol2/s3-practice/tree/parallel)

[CompletableFuture](https://github.com/xxeol2/s3-practice/tree/completablefuture)


## 주요 클래스 설명
![image](https://github.com/xxeol2/s3-practice/assets/71129059/b6d44af6-a8b5-4313-89f1-0a6c07ce1223)

- `PostFacadeService`는 전체 이미지 파일 업로드를 `ImageStorageService`에 요청합니다.
- `ImageStorageService`는 각 이미지 파일 업로드를 `StorageClient`에 요청합니다.
- 이미지 업로드가 성공하면, `PostFacadeService`는 포스트 저장을 `PostService`에 요청합니다.

## 설정 (application.yml)

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:13306/sns
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
    show-sql: true
    hibernate:
      ddl-auto: validate
  servlet:
    multipart:
      max-file-size: 1MB
      max-request-size: 10MB

cloud:
  aws:
    s3:
      bucket: {your-bucket-name}
    credentials:
      access-key: {your-access-key}
      secret-key: {your-secret-key}
    region:
      static: ap-northeast-2
      auto: false
    stack:
      auto: false

s3:
  base-url: {your-s3-bucket-base-url}

```

위 설정에서 {your-bucket-name}, {your-access-key}, {your-secret-key}, {your-s3-bucket-base-url}는 본인의 AWS 환경에 따라 적절히 변경하여 사용하면 됩니다.
