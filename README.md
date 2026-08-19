# Batch_Processor
Spring Boot와 MyBatis 기반으로 구현한 사용자/부서 정보 동기화 Batch 프로젝트입니다.

외부 시스템과 내부 시스템의 사용자 및 부서 데이터를 주기적으로 조회하고 비교하여,
신규 생성, 정보 수정, 퇴사 및 비활성 상태 등의 변경 사항을 자동으로 동기화하도록 구성했습니다.

Quartz Scheduler를 이용한 주기적인 Batch 실행과 다중 DB 연동, 데이터 정합성 비교, 예외 처리 및 Logging, JAR 기반 배포 및 실행 환경까지
실제 운영 환경을 고려한 Batch 시스템 형태로 구성했습니다.


▶ Project Overview

본 프로젝트는 외부 시스템과 내부 시스템 간 사용자 및 부서 정보의
데이터 정합성을 유지하기 위한 Batch 처리 구조를 구현한 프로젝트입니다.

주요 기능은 다음과 같습니다.

- 외부/내부 DB 사용자 정보 조회
- 외부/내부 DB 부서 정보 조회
- 사용자/부서 PK 기반 데이터 비교
- 신규 사용자/부서 생성
- 변경된 사용자/부서 정보 수정
- 퇴사 사용자 및 비활성 데이터 상태 처리
- Quartz Scheduler 기반 주기적 동기화
- 동기화 결과 성공/실패 건수 집계
- 예외 발생 시 Logging 및 장애 확인
- 외부 설정 파일을 이용한 DB 및 실행 환경 관리
- JAR 파일 기반 Batch 배포
- Linux / Windows 실행 및 종료 Script 구성


▶ Tech Stack

- Language : JAVA 8
- Framework : Spring Boot 2.7.18
- Build : Maven
- Database : Oracle
- DB Access : MyBatis
- Scheduler : Quartz Scheduler
- Configuration : YAML, XML
- Logging : SLF4J
- Deployment : JAR
- OS Script : Linux Shell, Windows Batch


▶ Project Structure

```text
Batch
├── conf
│   ├── application.yaml
│   └── quartz_scheduler.xml
│
├── jar
│   └── batch.jar
│
├── src
│   └── main
│       ├── java
│       │   ├── scheduler
│       │   │   ├── UserSyncJob.java
│       │   │   └── GroupSyncJob.java
│       │   │
│       │   ├── synchronizer
│       │   │   ├── UserSynchronizer.java
│       │   │   └── GroupSynchronizer.java
│       │   │
│       │   ├── service
│       │   │   ├── UserService.java
│       │   │   └── GroupService.java
│       │   │
│       │   ├── repository
│       │   │   ├── external
│       │   │   └── internal
│       │   │
│       │   ├── domain
│       │   └── BatchApplication.java
│       │
│       └── resources
│           └── db
│               └── repository
│
├── log
│   └── batch.log
│
├── pid
│   └── batch.pid
│
├── run.sh
├── stop.sh
├── run.bat
└── stop.bat


▶ Layer Description

- BatchApplication
  - Spring Boot Batch Application의 실행을 담당합니다.
  - Scheduler 및 Spring Bean 초기화를 수행합니다.

- Scheduler Job
  - Quartz Scheduler에 의해 실행되는 작업 단위입니다.
  - 실제 동기화 로직은 Synchronizer에 위임하고 실행 결과 및 오류를 Logging 합니다.

- Synchronizer
  - 외부/내부 데이터를 비교하고 동기화 대상 데이터를 분류합니다.
  - 신규 생성, 정보 수정, 퇴사 및 비활성 상태 등의 처리 대상을 구분합니다.

- Service
  - 생성/수정/상태 변경 등의 실제 비즈니스 로직을 처리합니다.
  - Repository를 호출하여 DB 변경 작업을 수행합니다.

- Repository
  - MyBatis를 이용하여 External DB 및 Internal DB에 접근합니다.
  - Interface와 XML Mapper를 분리하여 SQL을 관리합니다.

- Domain
  - User, Group 등 사용자 및 부서 관련 데이터 객체를 관리합니다.


▶ Batch Processing Flow

외부 시스템과 내부 시스템의 데이터를 조회한 후
PK를 기준으로 데이터를 비교하여 변경 사항을 분류합니다.

```text
External DB
    ↓
External User / Group 조회
    ↓
Internal DB 조회
    ↓
PK 기반 데이터 비교
    ↓
┌───────────────┬───────────────┬─────────────────┐
│ 신규 데이터    │ 변경 데이터    │ 상태 변경 데이터 │
│ CREATE        │ UPDATE        │ RETIRE/INACTIVE │
└───────────────┴───────────────┴─────────────────┘
        ↓
Service
        ↓
Internal DB 반영
        ↓
처리 결과 집계 및 Logging


▶ User Synchronization

사용자 정보는 외부 시스템과 내부 시스템의 User ID를 기준으로
데이터를 비교하여 동기화하도록 구성했습니다.

1. External DB에서 사용자 목록 조회
2. Internal DB에서 사용자 목록 조회
3. User ID(PK)를 기준으로 사용자 존재 여부 확인
4. Internal DB에 존재하지 않는 사용자는 신규 생성 대상 처리
5. 기존 사용자는 사용자 정보 비교
6. User Name, Password, Email, Phone, GroupId 변경 여부 확인
7. 변경된 사용자는 UPDATE 대상 처리
8. Employ_Status 변경 여부 확인
9. 외부 사용자 상태가 퇴사 상태인 경우 RETIRE 대상 처리
10. Service를 통해 실제 DB 반영
11. 처리 성공/실패 건수 집계


▶ Group Synchronization

부서 정보 역시 사용자 동기화와 동일한 패턴으로 구성하여
외부 시스템과 내부 시스템 간 데이터 정합성을 유지하도록 구현했습니다.

1. External DB에서 부서 목록 조회
2. Internal DB에서 부서 목록 조회
3. Group ID(PK)를 기준으로 부서 존재 여부 확인
4. 존재하지 않는 부서는 신규 생성 대상 처리
5. 기존 부서는 부서명 및 관련 정보 비교
6. 변경된 부서는 UPDATE 대상 처리
7. 비활성 또는 변경된 상태의 부서 처리
8. Service를 통한 Internal DB 반영
9. 처리 결과 집계 및 Logging


▶ Quartz Scheduler

Quartz Scheduler를 이용하여 사용자 및 부서 동기화 작업을
주기적으로 실행하도록 구성했습니다.

User Synchronization

- Job : userSyncJob
- Trigger : userSyncTrigger
- 실행 주기 : 10분마다

Group Synchronization

- Job : groupSyncJob
- Trigger : groupSyncTrigger
- 실행 주기 : 5분부터 10분 간격

사용자와 부서 동기화 작업이 동시에 실행되지 않도록
실행 시간을 분리하여 구성했습니다.


▶ Configuration

실행 환경과 DB 접속 정보는 소스 코드와 분리하여
외부 설정 파일에서 관리하도록 구성했습니다.

conf/application.yaml

- External DB Connection
- Internal DB Connection
- MyBatis 설정
- Logging 설정
- Batch 실행 환경 설정

conf/quartz_scheduler.xml

- User Synchronization Job
- Group Synchronization Job
- Trigger
- Cron Expression


▶ External / Internal DB

본 프로젝트는 서로 다른 시스템의 DB를 연동하는 상황을 고려하여
External DB와 Internal DB를 분리하여 구성했습니다.

External DB

- 연계 시스템에서 사용자 및 부서 정보를 조회
- 원천 데이터 역할

Internal DB

- 내부 시스템의 사용자 및 부서 정보 관리
- 동기화 결과 반영

이를 통해 외부 시스템의 변경 사항을 내부 시스템에
주기적으로 반영하는 구조를 구현했습니다.


▶ Data Consistency

동기화 과정에서 데이터 정합성을 유지하기 위해
각 데이터의 PK를 기준으로 비교하도록 구성했습니다.

User

- User ID

Group

- Group ID

PK를 기준으로 존재 여부를 먼저 확인한 후,
기존 데이터에 대해서만 상세 필드의 변경 여부를 비교하도록 구성했습니다.

이를 통해 신규 데이터와 기존 데이터의 처리 로직을 분리하고
불필요한 UPDATE 작업을 최소화하도록 설계했습니다.


▶ Exception Handling

Batch 실행 중 발생할 수 있는 예외를 Logging하여
운영 과정에서 장애 원인을 확인할 수 있도록 구성했습니다.

- DB 조회 및 저장 과정의 Exception 처리
- 사용자/부서 동기화 과정의 오류 Logging
- 개별 데이터 처리 실패 확인
- Batch 전체 실행 실패 Logging
- 처리 성공/실패 건수 집계

오류 발생 시 Log를 기반으로 원인을 확인하고
실패 데이터에 대한 재처리를 고려할 수 있도록 구성했습니다.


▶ Logging

Batch 실행 과정에서 주요 처리 상태를 Logging 합니다.

예시:

- Batch Start
- User Synchronization Start
- Group Synchronization Start
- CREATE / UPDATE / RETIRE 처리 결과
- 처리 성공 건수
- 처리 실패 건수
- Exception 및 Stack Trace
- Batch Completion

실행 로그는 별도의 log 디렉토리에 저장하도록 구성했습니다.


▶ JAR Deployment

Maven을 이용하여 Batch Application을 JAR 형태로 Build하고
운영 환경에서 JAR 파일을 실행할 수 있도록 구성했습니다.

jar
└── batch.jar

외부 설정 파일을 사용하기 때문에 JAR 내부에 DB 접속 정보 등을
직접 포함하지 않고 conf/application.yaml을 별도로 사용합니다.

실행 시 다음 옵션을 이용하여 외부 설정 파일을 지정합니다.

--spring.config.additional-location=optional:file:./conf/application.yaml


▶ Linux / Windows Execution

Linux 환경

실행:

./run.sh

종료:

./stop.sh


Windows 환경

실행:

run.bat

종료:

stop.bat


실행 Script에서는 다음 작업을 수행합니다.

- JAR 파일 존재 여부 확인
- application.yaml 존재 여부 확인
- 중복 실행 여부 확인
- Batch Application 실행
- PID 관리
- Log 파일 생성
- Batch Application 종료


▶ Directory Management

운영 환경에서 생성되는 PID 및 Log 파일은
Git Repository에서 관리하지 않도록 구성했습니다.

.gitignore

/pid/
/log/

실제 운영 환경에서는 다음과 같이 생성됩니다.

pid
└── batch.pid

log
└── batch.log


▶ Project Purpose

본 프로젝트를 통해 다음과 같은 Backend 및 Batch 개발 경험을
개인 프로젝트 형태로 정리하고자 했습니다.

1. Spring Boot 기반 Batch Application 구성
2. Quartz Scheduler를 이용한 주기적인 작업 실행
3. External / Internal DB 연동
4. MyBatis 기반 Repository 및 SQL Mapper 구성
5. 대량 데이터 동기화 처리
6. PK 기반 데이터 정합성 비교
7. CREATE / UPDATE / RETIRE 데이터 분류
8. Service / Synchronizer / Repository 계층 분리
9. Batch Exception Handling 및 Logging
10. 처리 결과 성공/실패 건수 관리
11. 외부 Configuration을 이용한 실행 환경 분리
12. JAR 기반 Application 배포
13. Linux / Windows 실행 Script 구성


▶ Architecture

```text
                    BatchApplication
                          │
                          ▼
                  Quartz Scheduler
                    │           │
                    ▼           ▼
              UserSyncJob   GroupSyncJob
                    │           │
                    ▼           ▼
            UserSynchronizer GroupSynchronizer
                    │           │
                    ▼           ▼
                UserService  GroupService
                    │           │
             ┌──────┴──────┐    │
             ▼             ▼    ▼
        External DB    Internal DB
             │             │
             └──────┬──────┘
                    ▼
             Sync Result
                    │
                    ▼
              Logging / PID
