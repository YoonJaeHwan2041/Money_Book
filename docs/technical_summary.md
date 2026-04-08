# 📑 Project MoneyBook: Technical Summary

## 1. Project Overview
*   **Goal**: 로컬 DB(Room) 기반의 가계부 및 N/1 정산 관리 안드로이드 애플리케이션
*   **Architecture**: MVVM + Clean Architecture (Domain-Driven Package Structure)
*   **Tech Stack**: Kotlin 2.0 (K2), Jetpack Compose, Hilt (DI), Room (DB), KSP

---

## 2. Technical Stack & Environment

| Category | Stack | Version / Details |
| :--- | :--- | :--- |
| **Language** | Kotlin | 2.0.21 (K2 Compiler) |
| **JDK** | Java | Java 17 |
| **DI** | Hilt | 2.51 |
| **Database** | Room | 2.6.1 |
| **Annotation** | KSP | 2.0.21-1.0.28 |
| **UI** | Compose | Material3, Navigation-Compose |

---

## 3. Database Schema (Room)

### 🏗️ Entity 관계 (ERD 구조)
*   **`CategoryEntity`**: 지출 카테고리 (식비, 교통비 등)
*   **`TransactionEntity`**: 가계부 지출/수입 내역 (`categoryId` 외래키 포함)
*   **`SplitMemberEntity`**: 정산이 필요한 거래에 대해 `transactionId`를 참조하는 1:N 관계의 멤버 리스트

### 🛠️ 주요 기능 (DAO)
*   **Flow 지원**: 모든 조회(Read) 쿼리는 `Flow<List<T>>`를 반환하여 실시간 UI 업데이트 지원
*   **비동기 처리**: 삽입, 수정, 삭제는 Coroutine `suspend fun`으로 처리

---

## 4. Architecture & Package Structure
Spring 개발자 관점에서 익숙한 **도메인 중심 구조**를 채택했습니다.

```plaintext
com.jaehwan.moneybook
├── common (공통 인프라)
│   ├── data.local (AppDatabase, TypeConverters)
│   └── di (DatabaseModule - Hilt 설정)
├── category (카테고리 도메인)
│   ├── data (Entity, Dao, Repository)
│   └── ui (ViewModel, Compose Screens)
├── transaction (지출 내역 도메인)
│   ├── data (Entity, Dao)
│   └── ui
└── settlement (정산 도메인)
    ├── data (SplitMember Entity, Dao)
    └── ui
```

---

## 5. Key Implementation Details

### 💉 의존성 주입 (Hilt)
*   **`@HiltAndroidApp`**: `MoneyBookApplication`을 통한 전역 컨텍스트 초기화
*   **`DatabaseModule`**: Singleton 범위의 `AppDatabase` 및 각 DAO 빈(Bean) 등록
*   **`@AndroidEntryPoint`**: `MainActivity` 등 안드로이드 컴포넌트에 의존성 주입 활성화

### 📊 UI & State Management
*   **ViewModel**: `StateFlow`와 `stateIn`을 활용해 DB의 `Flow`를 UI 상태로 변환
*   **Compose**: `collectAsState()`를 통해 데이터 변화에 따른 리액티브 UI 구현

---

## 6. Next Steps
- [ ] **Transaction 입력 UI**: 금액, 날짜, 카테고리 선택 기능 구현
- [ ] **N/1 정산 로직**: 특정 지출 건에 대해 멤버별 정산 상태 관리 기능
- [ ] **Data Validation**: 입력값 예외 처리 및 유틸리티 함수(날짜 포맷 등) 추가
