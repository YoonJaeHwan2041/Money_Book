# 🛠 빌드 환경 설정 및 트러블슈팅 기록 (Build Setup & Troubleshooting)

본 문서는 프로젝트 초기 환경 설정 과정에서 발생한 주요 기술적 이슈와 해결 과정을 기록합니다. Spring 개발 환경에서 Android Modern Development(MAD)로 전환하며 겪은 버전 관리 경험을 포함합니다.

---

## 1. Kotlin 2.0 & KSP 버전 호환성 이슈
### **[문제 상황]**
- `libs.versions.toml`에서 Kotlin 버전을 최신(`2.2.10` 등)으로 설정했으나, KSP(Kotlin Symbol Processing) 플러그인과 충돌하며 빌드가 중단됨.
- 에러 메시지: `KSP 2.0.21-1.0.28 is only compatible with Kotlin 2.0.21.`

### **[원인 분석]**
- **KSP의 특수성**: KSP는 코틀린 컴파일러에 직접 의존하므로, 사용하는 Kotlin 버전과 KSP의 배포 버전(앞자리)이 소수점 단위까지 완벽히 일치해야 함.
- **안정성 검토**: 지나치게 앞서가는 버전보다는 현재 Hilt와 Room이 공식적으로 가장 안정적으로 지원하는 Kotlin 2.0.21 버전이 적합하다고 판단.

### **[해결 방법]**
- **버전 동기화**: Kotlin 버전을 `2.0.21`로, KSP 버전을 `2.0.21-1.0.28`로 일치시켜 컴파일러 단계의 충돌을 해결.
- **K2 컴파일러 활용**: Kotlin 2.0의 새로운 K2 컴파일러 기능을 활용하여 빌드 속도를 최적화함.

---

## 2. Android SDK 및 Gradle 문법 오류
### **[문제 상황]**
- `app/build.gradle.kts` 파일의 `compileSdk` 설정에서 `release(36)`와 같은 비표준 문법 에러 발생 및 싱크 실패.

### **[원인 분석]**
- 안드로이드 스튜디오의 실험적 템플릿 코드와 현재 설치된 Android Gradle Plugin(AGP) 버전 간의 문법 불일치.

### **[해결 방법]**
- **표준화**: `compileSdk = 35`, `targetSdk = 35`로 명시적이고 안정적인 수치로 수정.
- **AGP 업데이트**: Android Gradle Plugin을 최신 안정 버전인 `8.7.2`로 업데이트하여 Gradle 8.x 환경과의 호환성 확보.

---

## 3. JVM 타겟 및 Java 버전 업그레이드 (Java 17)
### **[문제 상황]**
- 초기 설정인 Java 11 환경에서 최신 안드로이드 개발 표준 및 라이브러리 최적화 기능을 충분히 활용하지 못할 가능성 확인.

### **[해결 방법]**
- **Java 17 전환**: 프로젝트의 `sourceCompatibility`, `targetCompatibility`, 그리고 Kotlin의 `jvmTarget`을 모두 `JavaVersion.VERSION_17` (또는 `"17"`)로 업그레이드.
- **기대 효과**: Java 17의 최신 언어 기능(Sealed Classes, Records 등)을 지원하며, 최신 AGP(Android Gradle Plugin) 버전과의 완벽한 호환성을 보장함.

---

## 💡 기술적 인사이트 (Learning Points)
1. **버전 카탈로그(`toml`)의 중요성**: 라이브러리 버전을 한곳에서 관리함으로써 복잡한 의존성 관계를 한눈에 파악하고 수정할 수 있었음.
2. **KSP vs KAPT**: 기존 `kapt` 대신 `KSP`를 선택함으로써 빌드 성능을 개선하고, Kotlin 2.0 환경에 최적화된 어노테이션 프로세싱 환경을 구축함.
3. **Strict Versioning**: 안드로이드 생태계는 라이브러리 간 의존성이 매우 엄격하므로, 라이브러리 추가 시 공식 문서의 호환성 매트릭스를 확인하는 습관의 중요성을 인지함.