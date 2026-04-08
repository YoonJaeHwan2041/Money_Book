# Project Progress Summary (MoneyBook)

## 프로젝트 현황 요약 (최신 업데이트)

### 1. 기술 스택 현대화
- **언어 및 컴파일러**: Kotlin `2.0.21` (K2 컴파일러) 적용.
- **Java 버전**: 기존 Java 11에서 **Java 17**로 전면 업그레이드. (`compileOptions`, `kotlinOptions` 반영)
- **빌드 시스템**: AGP `8.7.2`, Gradle `9.x` 기반의 최신 설정 유지.

### 2. 라이브러리 및 DI/DB 설정 완료
- **Dependency Injection**: **Hilt (2.51)** 설정 완료 및 `MoneyBookApplication` 적용.
- **Database**: **Room (2.6.1)** 연동 완료. (Transaction, Category, SplitMember 테이블 구조 확인)
- **Annotation Processing**: Kapt를 대신하여 **KSP (2.0.21-1.0.28)**를 전체적으로 적용하여 빌드 속도 및 Kotlin 2.0 호환성 확보.
- **Compose Navigation**: Hilt와 Compose 연동을 위한 `hilt-navigation-compose` 추가.

### 3. 주요 이슈 해결 (Troubleshooting)
- **AndroidX 활성화**: `gradle.properties`에 `android.useAndroidX=true`가 누락되어 발생하던 모든 라이브러리 인식 오류(시뻘건 에러)를 해결.
- **Build Script 오류**: `compileSdk` 설정 문법 오류(`release(36)`)를 표준 방식(`35`)으로 수정하여 빌드 안정화.
- **Hilt 연동 에러**: `@AndroidEntryPoint` 추가 및 `hiltViewModel()` 사용을 위한 의존성 정렬 완료.

### 4. UI 구현 (MainActivity)
- **기능**: 저장된 카테고리 목록을 실시간으로 표시하고, 플로팅 액션 버튼(FAB)을 통해 샘플 데이터를 추가하는 기능을 구현.
- **기술**: Jetpack Compose, Material3, Flow (`collectAsState`), Hilt ViewModel 주입 적용.

---
## 향후 작업 제안
1. **거래 내역(Transaction) 화면 구현**: 현재 구축된 Room DB를 활용하여 실제 가계부 내역을 입력/조회하는 화면 개발.
2. **카테고리 관리 기능 확장**: 카테고리 삭제, 아이콘 선택 기능 추가.
3. **디자인 시스템 정교화**: `ui/theme` 폴더의 색상 및 타이포그래피 커스텀.
