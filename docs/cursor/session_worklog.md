# Cursor 작업 정리 (MoneyBook)

본 문서는 Cursor에서 진행한 **빌드/환경 이슈**, **UI·기능 추가**, **구조 리팩터링**을 한곳에 모아 둔 기록입니다. 기존 `docs/*.md`(설계·진행 보고)와는 별도로, **에이전트 세션에서 실제로 반영된 변경** 위주로 적습니다.

---

## 1. 개발 환경 관련

### 1.1 Android Studio Gradle JVM과 Cursor 경로

- 증상: `jlink`가 `~/.cursor/extensions/redhat.java/.../jre/21...` 경로에 없다는 오류.
- 원인: Gradle JVM 기준을 **Version 21** 등으로 두면 IDE가 번들 JRE를 골라 **불완전한 런타임**이 잡힐 수 있음.
- 권장: **Gradle JDK를 Embedded JDK 또는 JDK 17**으로 명시. Cursor 확장 경로는 Gradle JVM으로 쓰지 않기.

### 1.2 `collectAsState` 미해결 참조

- 증상: `MainActivity.kt`에서 `Unresolved reference 'collectAsState'`.
- 조치: `import androidx.compose.runtime.collectAsState` 추가.

---

## 2. 기능·UI (Drawer + 카테고리 CRUD)

### 2.1 Drawer 네비게이션

- Material3 **`ModalNavigationDrawer`** + **`ModalDrawerSheet`** + **`NavigationDrawerItem`**.
- 상단 **`Icons.Default.Menu`**로 서랍 열기/닫기.
- 구역: **카테고리**, **가계부(플레이스홀더)**.
- **FAB(카테고리 추가)**는 **카테고리** 화면에서만 표시.

### 2.2 카테고리 수정·삭제

- 목록 행에 **수정 / 삭제** 버튼.
- **추가·수정 겸용** 폼 다이얼로그: 기존 `iconKey` 파싱으로 이모지/갤러리/리소스 초기값 반영.
- 삭제 시 **확인 다이얼로그** + `TransactionEntity`의 **`onDelete = CASCADE`** 안내 문구.

### 2.3 데이터 계층

- `CategoryDao`의 `update` / `delete`를 사용하도록 **Repository·ViewModel**에 메서드 연결.

---

## 3. 구조 리팩터링 (유지보수·클린 아키텍처에 가깝게)

### 3.1 목표

- **한 파일(MainActivity)에 UI가 몰리지 않게** 분리.
- Spring에서 흔한 **인터페이스(포트) + 구현체(어댑터)** 패턴에 맞춰 **Repository 추상화**.

### 3.2 패키지·파일 구조 (요지)

| 구분 | 경로 |
|------|------|
| 앱 진입 | `MainActivity.kt` — `MoneyBookTheme` + `MoneyBookApp()` 만 |
| 루트 UI·Drawer | `ui/root/MoneyBookApp.kt`, `ui/root/MainDestination.kt` |
| 가계부 플레이스홀더 | `transaction/ui/LedgerPlaceholderScreen.kt` |
| 카테고리 UI | `category/ui/CategoryListScreen.kt`, `CategoryFormDialog.kt`, `CategoryIconDisplay.kt`, `CategoryIconModels.kt`, `CategoryViewModel.kt` |
| 도메인 포트 | `category/domain/repository/CategoryRepository.kt` (interface) |
| 데이터 구현 | `category/data/local/CategoryRepositoryImpl.kt` |
| Hilt 바인딩 | `di/CategoryRepositoryModule.kt` (`@Binds`) |

삭제된 파일: 기존 단일 구현체 `category/data/local/CategoryRepository.kt` (인터페이스+`Impl`로 대체).

### 3.3 의존 방향

- `CategoryViewModel` → **`CategoryRepository` 인터페이스** (domain).
- 구현체 `CategoryRepositoryImpl` → `CategoryDao` (data).

---

## 4. 참고·범위 밖

- 가계부 **실제 거래 UI·Navigation-Compose 그래프**는 아직 미구현(플레이스홀더만).
- `kotlinOptions` → `compilerOptions` 마이그레이션은 별 이슈로 문서화 가능.

---

## 요약

1. **Gradle/JDK**: IDE에서 Gradle JVM을 **명시적 JDK 17(또는 스튜디오 Embedded)** 로 두고, Cursor 번들 JRE 경로는 피할 것.  
2. **기능**: 왼쪽 **Drawer**로 카테고리/가계부 전환, 카테고리 **추가·수정·삭제** 및 삭제 시 CASCADE 안내.  
3. **구조**: `MainActivity`는 진입만 담당하고, **Drawer·스캐폴드는 `MoneyBookApp`**, 카테고리 UI는 **`category/ui`**, 가계부 자리는 **`transaction/ui`**, 저장소는 **`domain` 인터페이스 + `data` 구현체 + Hilt `@Binds`** 로 나눔.  
4. **Compose**: `StateFlow` 수집 시 **`collectAsState` import** 필요.

이후 작업 시 이 파일을 갱신하거나, 날짜별로 `docs/cursor/` 아래에 분할 문서를 추가하면 됩니다.
