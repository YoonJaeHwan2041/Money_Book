# Build Configuration Update (Hilt & Room)

## 개요
프로젝트의 의존성 주입(Hilt) 및 데이터베이스(Room) 설정을 완료하고, 빌드 안정성을 위해 Gradle 설정을 최적화했습니다.

## 주요 변경 사항

### 1. 버전 업데이트 및 라이브러리 추가 (`libs.versions.toml`)
- **Kotlin**: `2.0.21` (K2 컴파일러 대응)
- **KSP**: `2.0.21-1.0.28` (Kotlin 버전과 일치시켜 충돌 방지)
- **Hilt**: `2.51`
- **Room**: `2.6.1`
- **AGP (Android Gradle Plugin)**: `8.7.2` (최신 안정 버전)

### 2. Gradle 빌드 스크립트 수정
#### Root `build.gradle.kts`
- `hilt` 및 `ksp` 플러그인을 최상위 빌드 파일에 등록하여 하위 모듈에서 사용할 수 있도록 설정했습니다.

#### `app/build.gradle.kts`
- **오류 수정**: 기존 `compileSdk` 설정의 잘못된 문법(`release(36) { ... }`)을 표준 방식인 `35`로 수정하여 싱크 오류를 해결했습니다.
- **플러그인 적용**:
    - `com.google.dagger.hilt.android`
    - `com.google.devtools.ksp`
- **의존성 추가**:
    - **Hilt**: `hilt-android`, `hilt-compiler` (KSP 사용)
    - **Room**: `room-runtime`, `room-ktx`, `room-compiler` (KSP 사용)
- **JVM 타겟**: `kotlinOptions`를 통해 `jvmTarget = "17"`로 명시적 설정했습니다.

## Gemini 참고용 가이드
이 프로젝트는 이제 다음 기술 스택을 기반으로 합니다:
- **Language**: Kotlin 2.0.21 & Java 17
- **Dependency Injection**: Hilt
- **Annotation Processing**: KSP (kapt 대신 사용)
- **Database**: Room (with Kotlin Coroutines)
- **UI Framework**: Jetpack Compose (Kotlin 2.0.21 버전 사용)

코드를 생성하거나 수정할 때 위 스택을 기준으로 제안해 주세요.
