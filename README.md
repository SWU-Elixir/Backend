# Elixir Backend

- 서울여자대학교 소프트웨어융합학과 2025 프로젝트종합설계I
- 저속노화를 위한 개인 맞춤형 식단 관리 서비스, 엘릭서 프로젝트의 백엔드 레포지토리입니다.

<br>

## 🏗 Architecture
<img width="2048" height="1359" alt="image" src="https://github.com/user-attachments/assets/0ac03689-04b1-4fa2-b74e-a304ce847e1b" />

<br>

## 🛠 Tech Stack

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot
- **ORM:** Spring Data JPA
- **Security:** Spring Security, JWT, OAuth 2.0
- **Build Tool:** Gradle

### Database
- MySQL
- Redis

### Infrastructure
- **Cloud (AWS):** ECS Fargate, ALB, RDS, S3, Route 53, ACM, IAM, VPC
- **Container:** Docker
- **CI/CD:** GitHub Actions, Amazon ECR

### Testing
- JUnit5

### Tools
- Git
- GitHub
- Postman
- Swagger

<br>

## 📂 ERD
<img width="1633" height="1242" alt="image" src="https://github.com/user-attachments/assets/d57acf84-ccb1-4ed6-82de-6a0ecfbd866f" />

<br>

## 🚀 Deployment
GitHub Actions와 Amazon ECS(Fargate)를 활용하여 빌드부터 컨테이너 배포까지의 전 과정을 자동화했습니다.

### CI/CD Pipeline
``` text
Developer ──(Push)──> GitHub Actions ──(Build Docker Image)──> Amazon ECR
                            │                                        │
                       (Update Task Definition)                  (Pull Image)
                            ▼                                        ▼
                      Amazon ECS (Fargate) <─── ALB ─── Route 53 (HTTPS)
```

1. GitHub Actions가 애플리케이션을 빌드하고 Docker 이미지를 생성합니다.
2. 생성된 이미지를 Amazon ECR에 푸시한 뒤, ECS Task Definition의 이미지 정보를 최신 버전으로 갱신합니다.
3. Amazon ECS(Fargate)가 새로운 Task를 실행하여 서비스를 무중단으로 배포하며, ALB와 Route 53, ACM을 통해 HTTPS 기반으로 서비스를 제공합니다.

<br>

## 👥 Team

| [김서연](https://github.com/seoyeon83)  | [최민정](https://github.com/Acho-mj) |
|:------------------------------:|:------------------------------:|
| <img src="https://avatars.githubusercontent.com/u/53962059?v=4" width="200" height="200">  | <img src="https://avatars.githubusercontent.com/u/97894470?v=4" width="200" height="200">|
