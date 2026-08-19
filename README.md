# Batch_Processor

Spring Boot와 MyBatis 기반으로 구현한 **사용자 / 부서 정보 동기화 Batch 프로젝트**입니다.

외부 시스템과 내부 시스템의 사용자 및 부서 데이터를 주기적으로 조회하고 비교하여,
신규 생성, 정보 수정, 퇴사 및 비활성 상태 등의 변경 사항을 자동으로 동기화하도록 구성했습니다.

Quartz Scheduler를 이용한 주기적인 Batch 실행과 다중 DB 연동, PK 기반 데이터 정합성 비교, 예외 처리 및 Logging,
외부 Configuration을 이용한 실행 환경 분리, JAR 기반 배포 및 Linux / Windows 실행 Script까지
실제 운영 환경을 고려한 Batch Application 형태로 구성했습니다.

---

# ▶ Project Overview

본 프로젝트는 외부 시스템과 내부 시스템 간 사용자 및 부서 정보의 **데이터 정합성을 유지하기 위한 주기적 동기화 Batch**를 구현한 프로젝트입니다.

External DB를 원천 데이터로 사용하고,
Internal DB의 현재 데이터와 비교하여 변경된 데이터를 분류한 후
CREATE / UPDATE / RETIRE 등의 작업을 수행하도록 구성했습니다.

### 주요 기능

* External / Internal DB 사용자 정보 조회
* External / Internal DB 부서 정보 조회
* User ID / Group ID 기반 데이터 존재 여부 비교
* 신규 사용자 / 부서 생성
* 변경된 사용자 / 부서 정보 수정
* 퇴사 사용자 상태 처리
* 비활성 부서 및 상태 변경 데이터 처리
* Quartz Scheduler 기반 주기적 동기화
* 동기화 결과 성공 / 실패 건수 집계
* Batch 실행 과정 Logging
* Exception 발생 시 오류 Logging
* 외부 Configuration을 이용한 DB 및 실행 환경 관리
* JAR 파일 기반 Batch 배포
* Linux / Windows 실행 및 종료 Script 구성
* PID 기반 Batch Process 관리

---

# ▶ Architecture

본 프로젝트는 **Scheduler → Synchronizer → Service → Repository** 구조로 역할을 분리하여
Batch 실행 제어와 실제 데이터 동기화 로직을 분리했습니다.

```text
                         BatchApplication
                                │
                                ▼
                        Quartz Scheduler
                           │         │
                           │         │
                ┌──────────┘         └──────────┐
                ▼                               ▼
          UserSyncJob                       GroupSyncJob
                │                               │
                ▼                               ▼
        UserSynchronizer                GroupSynchronizer
                │                               │
                ▼                               ▼
           UserService                    GroupService
                │                               │
                ▼                               ▼
        ┌───────────────┐               ┌───────────────┐
        │  Repository   │               │  Repository   │
        └───────┬───────┘               └───────┬───────┘
                │                               │
          ┌─────┴─────┐                   ┌─────┴─────┐
          ▼           ▼                   ▼           ▼
     External DB  Internal DB        External DB  Internal DB
          │           │                   │           │
          └─────┬─────┘                   └─────┬─────┘
                │                               │
                └──────────────┬────────────────┘
                               ▼
                     Synchronization Result
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
                 Logging              Result Count
```

### Batch Processing Flow

전체 Batch 처리 흐름은 다음과 같습니다.

```text
Batch Application Start
          │
          ▼
   Quartz Scheduler
          │
          ├─────────────────────┐
          ▼                     ▼
    User Sync Job         Group Sync Job
          │                     │
          ▼                     ▼
 External Data 조회       External Data 조회
          │                     │
          ▼                     ▼
 Internal Data 조회       Internal Data 조회
          │                     │
          ▼                     ▼
      PK 기준 비교           PK 기준 비교
          │                     │
          ▼                     ▼
 ┌────────┼────────┐      ┌─────┼────────┐
 ▼        ▼        ▼      ▼     ▼        ▼
CREATE  UPDATE   RETIRE  CREATE UPDATE  INACTIVE
 └────────┼────────┘      └─────┼────────┘
          │                     │
          ▼                     ▼
        Service               Service
          │                     │
          ▼                     ▼
      Internal DB 반영       Internal DB 반영
          │                     │
          └──────────┬──────────┘
                     ▼
              성공 / 실패 집계
                     │
                     ▼
                  Logging
                     │
                     ▼
              Batch Processing End
```

---

# ▶ Project Structure

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
│       │   │
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
```

---

# ▶ Layer Description

## BatchApplication

Spring Boot Application의 시작점입니다.

* Spring Application 실행
* Application Context 초기화
* Scheduler 및 Spring Bean 초기화
* 외부 Configuration 적용

```text
BatchApplication
       │
       ▼
Spring Application Context
       │
       ├── Scheduler
       ├── Service
       ├── Repository
       └── Configuration
```

---

## Scheduler Job

Quartz Scheduler에 의해 실행되는 **Batch 작업 단위**입니다.

실제 데이터 동기화 로직을 직접 처리하지 않고, UserSynchronizer / GroupSynchronizer에 작업을 위임하도록 구성했습니다.

```text
Quartz Trigger
      │
      ▼
UserSyncJob / GroupSyncJob
      │
      ▼
Synchronizer
```

이를 통해 Scheduler의 역할과 실제 동기화 비즈니스 로직을 분리했습니다.

---

## Synchronizer

External DB와 Internal DB의 데이터를 비교하고 동기화 대상 데이터를 분류하는 핵심 영역입니다.

주요 역할은 다음과 같습니다.

* External 데이터 조회
* Internal 데이터 조회
* PK 기반 데이터 존재 여부 비교
* 신규 데이터 분류
* 변경 데이터 분류
* 퇴사 / 비활성 데이터 분류
* Service 호출
* 처리 결과 집계

```text
External Data
     │
     ▼
Internal Data
     │
     ▼
PK 기준 비교
     │
     ├── CREATE
     ├── UPDATE
     └── RETIRE / INACTIVE
```

---

## Service

실제 데이터 생성 / 수정 / 상태 변경 등의 비즈니스 로직을 담당합니다.

Synchronizer에서 분류한 처리 대상을 전달받아
Repository를 통해 Internal DB에 변경 사항을 반영합니다.

```text
Synchronizer
     │
     ▼
Service
     │
     ▼
Repository
     │
     ▼
Internal DB
```

---

## Repository

MyBatis를 이용하여 External DB 및 Internal DB에 접근합니다.

Repository Interface와 XML Mapper를 분리하여
Java 코드와 SQL을 분리하여 관리했습니다.

```text
Repository Interface
        │
        ▼
MyBatis XML Mapper
        │
        ▼
       SQL
        │
        ▼
 External / Internal DB
```

---

## Domain

Batch 처리 과정에서 사용하는 데이터 객체를 관리합니다.

주요 객체 예시는 다음과 같습니다.

* User
* Group
* Synchronization Result

---

# ▶ User Synchronizer

사용자 정보는 **User ID(PK)**를 기준으로 External DB와 Internal DB의 데이터를 비교하여 동기화하도록 구성했습니다.

### 처리 과정

```text
External User 조회
        │
        ▼
Internal User 조회
        │
        ▼
User ID(PK) 비교
        │
        ├───────────────┐
        ▼               ▼
  존재하지 않음       이미 존재
        │               │
        ▼               ▼
      CREATE       상세 정보 비교
                        │
                        ├── 변경 없음
                        │
                        ├── 정보 변경
                        │       ↓
                        │     UPDATE
                        │
                        └── 퇴사 상태
                                ↓
                              RETIRE
```

### 상세 처리

1. External DB에서 사용자 목록 조회
2. Internal DB에서 사용자 목록 조회
3. User ID(PK)를 기준으로 사용자 존재 여부 확인
4. Internal DB에 존재하지 않는 사용자는 CREATE 대상 처리
5. 기존 사용자는 상세 정보 비교
6. User Name / Password / Email / Phone / Group ID 변경 여부 확인
7. 변경된 사용자는 UPDATE 대상 처리
8. Employ Status 변경 여부 확인
9. 외부 사용자가 퇴사 상태인 경우 RETIRE 대상 처리
10. Service를 통해 Internal DB 반영
11. 처리 성공 / 실패 건수 집계
12. 처리 결과 Logging

---

# ▶ Group Synchronizer

부서 정보 역시 **Group ID(PK)**를 기준으로 External DB와 Internal DB의 데이터를 비교하여 동기화하도록 구성했습니다.

### 처리 과정

```text
External Group 조회
        │
        ▼
Internal Group 조회
        │
        ▼
Group ID(PK) 비교
        │
        ├───────────────┐
        ▼               ▼
  존재하지 않음       이미 존재
        │               │
        ▼               ▼
      CREATE       상세 정보 비교
                        │
                        ├── 변경 없음
                        │
                        ├── 정보 변경
                        │       ↓
                        │     UPDATE
                        │
                        └── 비활성 상태
                                ↓
                             INACTIVE
```

### 상세 처리

1. External DB에서 부서 목록 조회
2. Internal DB에서 부서 목록 조회
3. Group ID(PK)를 기준으로 부서 존재 여부 확인
4. 존재하지 않는 부서는 CREATE 대상 처리
5. 기존 부서는 부서명 및 관련 정보 비교
6. 변경된 부서는 UPDATE 대상 처리
7. 비활성 또는 상태 변경 부서 처리
8. Service를 통한 Internal DB 반영
9. 처리 성공 / 실패 건수 집계
10. 처리 결과 Logging

---

# ▶ Data Synchronization

본 프로젝트에서는 External DB를 원천 데이터로 사용하고,
Internal DB의 현재 데이터와 비교하여 변경 사항을 반영합니다.

```text
                 External System
                       │
                       ▼
                  External DB
                       │
                  Source Data
                       │
                       ▼
              ┌─────────────────┐
              │  Synchronizer   │
              │                 │
              │  PK 비교        │
              │  필드 비교      │
              │  상태 비교      │
              └────────┬────────┘
                       │
                       ▼
                  Internal DB
                       │
                       ▼
              Synchronization
                  Completed
```

---

# ▶ Data Consistency

동기화 과정에서 데이터 정합성을 유지하기 위해 각 데이터의 PK를 기준으로 존재 여부를 먼저 확인하도록 구성했습니다.

```text
User  → User ID
Group → Group ID
```
PK를 기준으로 신규 데이터와 기존 데이터를 먼저 구분한 후, 기존 데이터에 대해서만 상세 필드의 변경 여부를 비교합니다.

```text
PK 존재 여부 확인
       │
       ├── 없음
       │    ↓
       │  CREATE
       │
       └── 있음
            ↓
       상세 필드 비교
            │
            ├── 동일
            │    ↓
            │  Skip
            │
            └── 변경
                 ↓
               UPDATE
```

이를 통해 신규 데이터와 기존 데이터의 처리 로직을 분리하고,
변경 사항이 없는 데이터에 대한 불필요한 UPDATE 작업을 최소화하도록 설계했습니다.

---

# ▶ Quartz Scheduler

Quartz Scheduler를 이용하여 사용자 및 부서 동기화 작업을 주기적으로 실행하도록 구성했습니다.

### User Synchronization

```text
Job     : userSyncJob
Trigger : userSyncTrigger
주기    : 10분마다
```

### Group Synchronization

```text
Job     : groupSyncJob
Trigger : groupSyncTrigger
주기    : 설정된 Cron Expression에 따라 실행
```

사용자와 부서 동기화 작업의 실행 시간을 분리하여 두 작업이 동시에 실행되는 상황을 최소화하도록 구성했습니다.
실행 주기는 `conf/quartz_scheduler.xml`에서 관리하도록 구성했습니다.

---

# ▶ Configuration

실행 환경과 DB 접속 정보를 소스 코드와 분리하여 외부 Configuration 파일에서 관리하도록 구성했습니다.

## application.yaml

```text
conf/application.yaml
```

주요 설정:
* External DB Connection
* Internal DB Connection
* MyBatis 설정
* Logging 설정
* Batch 실행 환경 설정

DB 접속 정보와 같은 환경별 설정을 JAR 내부에서 분리하여 개발 / 테스트 / 운영 환경에 따라 설정을 변경할 수 있도록 구성했습니다.

---

## quartz_scheduler.xml

```text
conf/quartz_scheduler.xml
```

주요 설정:

* User Synchronizer Job
* Group Synchronizer Job
* Trigger
* Cron Expression
* Scheduler 실행 설정

---

# ▶ External / Internal DB

본 프로젝트는 서로 다른 시스템의 DB를 연동하는 환경을 고려하여 External DB와 Internal DB를 분리하여 구성했습니다.

### External DB

* 연계 시스템의 사용자 정보 조회
* 연계 시스템의 부서 정보 조회
* 동기화 대상의 원천 데이터 역할

### Internal DB

* 내부 시스템의 사용자 정보 관리
* 내부 시스템의 부서 정보 관리
* 동기화 결과 반영

```text
External DB
   │
   │ Source Data
   ▼
Synchronizer
   │
   │ CREATE / UPDATE / RETIRE
   ▼
Internal DB
```

---

# ▶ Batch Result

Batch 실행 결과는 처리 건수를 집계하여 실행 결과를 확인할 수 있도록 구성했습니다.

```text
User Synchronizer
 ├─ CREATE : N
 ├─ UPDATE : N
 ├─ RETIRE : N
 ├─ SUCCESS: N
 └─ FAIL   : N

Group Synchronizer
 ├─ CREATE : N
 ├─ UPDATE : N
 ├─ INACTIVE : N
 ├─ SUCCESS: N
 └─ FAIL   : N
```

이를 통해 단순히 Batch 작업의 성공 / 실패 여부만 확인하는 것이 아니라
실행 과정에서 실제로 몇 건의 데이터가 변경되었는지 확인할 수 있도록 구성했습니다.

---

# ▶ Exception Handling

Batch 실행 중 발생할 수 있는 예외를 Logging하여 운영 과정에서 장애 원인을 확인할 수 있도록 구성했습니다.

### 주요 예외 처리 대상

* DB 조회 과정 Exception
* DB Insert / Update 과정 Exception
* 사용자 동기화 과정 Exception
* 부서 동기화 과정 Exception
* 개별 데이터 처리 실패
* Batch 전체 실행 실패

처리 과정에서 오류가 발생하면 Log를 통해 원인을 확인할 수 있도록 구성하고,
성공 / 실패 건수를 집계하여 Batch 실행 결과를 확인할 수 있도록 했습니다.

```text
Batch Start
    │
    ▼
Data Processing
    │
    ├── Success
    │     ↓
    │   Success Count
    │
    └── Exception
          ↓
       Error Logging
          ↓
       Fail Count
```

---

# ▶ Logging

Batch 실행 과정에서 주요 처리 상태를 Logging합니다.

### 주요 Log

* Batch Start
* User Synchronizer Start
* Group Synchronizer Start
* CREATE 처리 결과
* UPDATE 처리 결과
* RETIRE 처리 결과
* INACTIVE 처리 결과
* 성공 건수
* 실패 건수
* Exception 및 Stack Trace
* Batch Completion

실행 로그는 별도의 `log` 디렉토리에 저장하도록 구성했습니다.

```text
log
└── batch.log
```

---

# ▶ JAR Deployment

Maven을 이용하여 Batch Application을 JAR 형태로 Build하고 운영 환경에서 JAR 파일을 실행할 수 있도록 구성했습니다.

```text
jar
└── batch.jar
```

JAR 내부에 환경별 DB 접속 정보 및 실행 설정을 직접 포함하지 않고 외부 Configuration 파일을 사용하도록 구성했습니다.

```text
Batch
├── jar
│   └── batch.jar
│
└── conf
    ├── application.yaml
    └── quartz_scheduler.xml
```

실행 시 외부 설정 파일을 지정하여 Application을 실행합니다.

```text
--spring.config.additional-location=optional:file:./conf/application.yaml
```

이를 통해 JAR 파일을 동일하게 유지하면서 환경별 Configuration만 변경할 수 있도록 구성했습니다.

---

# ▶ Linux / Windows Execution

Linux와 Windows 환경에서 Batch Application을 실행할 수 있도록 OS별 실행 / 종료 Script를 구성했습니다.

## Linux

```text
실행
./run.sh

종료
./stop.sh
```

## Windows

```text
실행
run.bat

종료
stop.bat
```

### Script 처리

실행 Script에서는 다음 작업을 수행합니다.

```text
Script 실행
    │
    ├─ JAR 존재 여부 확인
    │
    ├─ application.yaml 확인
    │
    ├─ 중복 실행 여부 확인
    │
    ├─ PID 확인
    │
    ├─ Batch Application 실행
    │
    └─ Log 생성
```

종료 Script에서는 PID 파일을 이용하여 실행 중인 Batch Process를 확인하고 종료하도록 구성했습니다.

```text
pid
└── batch.pid
```

---

# ▶ Runtime Structure

실제 실행 환경에서는 다음과 같은 구조를 사용합니다.

```text
Batch
│
├── conf
│   ├── application.yaml
│   └── quartz_scheduler.xml
│
├── jar
│   └── batch.jar
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
```

Application 실행 파일, Configuration, Log, PID를 각각 분리하여
운영 시 필요한 파일을 관리하기 쉽도록 구성했습니다.

---

# ▶ End-to-End Processing

본 프로젝트의 전체적인 처리 흐름은 다음과 같습니다.

```text
              ┌─────────────────────┐
              │   Batch Application  │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │  Quartz Scheduler   │
              └──────────┬──────────┘
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
        UserSyncJob             GroupSyncJob
              │                     │
              ▼                     ▼
      UserSynchronizer      GroupSynchronizer
              │                     │
              ▼                     ▼
         UserService            GroupService
              │                     │
              ▼                     ▼
         Repository              Repository
              │                     │
       ┌──────┴──────┐       ┌──────┴──────┐
       ▼             ▼       ▼             ▼
 External DB    Internal DB  External DB  Internal DB
       │             │       │             │
       └──────┬──────┘       └──────┬──────┘
              │                     │
              └──────────┬──────────┘
                         ▼
                  Data Synchronization
                         │
                         ▼
                CREATE / UPDATE /
                RETIRE / INACTIVE
                         │
                         ▼
                 Success / Fail Count
                         │
                         ▼
                      Logging
                         │
                         ▼
                  Batch Completion
```

---

# ▶ Project Purpose

본 프로젝트를 통해 다음과 같은 Backend 및 Batch 개발 경험을 개인 프로젝트 형태로 정리하고자 했습니다.

1. Spring Boot 기반 Batch Application 구성
2. Quartz Scheduler를 이용한 주기적인 작업 실행
3. External / Internal DB 연동
4. MyBatis 기반 Repository 및 SQL Mapper 구성
5. 사용자 / 부서 데이터 동기화 처리
6. PK 기반 데이터 정합성 비교
7. CREATE / UPDATE / RETIRE / INACTIVE 데이터 분류
8. Scheduler / Synchronizer / Service / Repository 역할 분리
9. Batch Exception Handling 및 Logging
10. 처리 결과 성공 / 실패 건수 관리
11. 외부 Configuration을 이용한 실행 환경 분리
12. JAR 기반 Application 배포
13. Linux / Windows 실행 Script 구성
14. PID 및 Log를 이용한 Batch Process 관리

---

# ▶ Key Learning

본 프로젝트에서는 단순히 Scheduler를 이용하여 특정 작업을 반복 실행하는 것에 그치지 않고,

**데이터 조회 → 비교 → 변경 대상 분류 → DB 반영 → 결과 집계 → Logging**

으로 이어지는 전체적인 Batch 처리 흐름을 구성했습니다.

```text
External Data
      ↓
Internal Data
      ↓
PK Based Comparison
      ↓
Change Detection
      ↓
CREATE / UPDATE / RETIRE / INACTIVE
      ↓
Service
      ↓
Repository / MyBatis
      ↓
Internal DB
      ↓
Success / Fail Count
      ↓
Logging
```

또한 Application 실행 환경과 소스 코드를 분리하기 위해 외부 Configuration 파일을 사용하고,
JAR 기반 배포와 Linux / Windows 실행 Script를 구성하여
실제 운영 환경에서 Batch Application을 실행하고 관리하는 구조까지 함께 구현했습니다.

---

# ▶ Author

**안재호**

Java Backend Developer

