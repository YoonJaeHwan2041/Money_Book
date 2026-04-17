# 2026-04-15 유지보수성 개선 작업 기록

## 목표
- 기능은 유지하면서 코드 구조를 유지보수 관점으로 정리
- 중복 정책 제거, 상태 동기화 안정화, 테스트 기반 추가

## 이번 작업 핵심

### 1) 루트 상태 단순화 (`MoneyBookApp`)
- 설정 화면 내부 상태를 다중 boolean 대신 `SettingsSection` enum으로 통합
  - `Root`, `CategoryManager`, `LegacyLedger`
- 뒤로가기 처리 시 설정 내부 상태를 단일 규칙으로 복귀하도록 정리

### 2) 공통 정책 유틸 추출
- 신규 파일: `transaction/domain/MoneyUiPolicy.kt`
  - `parseMoneyInput`
  - `formatMoneyInput`
  - `formatMoney`
  - `isSplitComplete`
  - `unpaidTotal`
- 기존 여러 UI 파일의 private 중복 함수 호출을 공통 정책으로 이관

### 3) 화면 계산 로직 분리
- 신규 파일: `transaction/ui/LedgerTradePolicies.kt`
  - `isInMonth`
  - `calculateMonthlyTotals`
  - `calculateCurrentBalance`
- `LedgerScreen`, `TradeScreen`에서 화면 내부 계산 일부를 정책 함수 사용으로 변경

### 4) 거래 입력 검증 분리
- 신규 파일: `transaction/ui/TransactionEntryValidation.kt`
  - `validateTransactionAmount`
  - `validateSplitInput`
- `TransactionEntryScreen` 저장 시 검증 문구 생성을 전용 함수로 분리

### 5) 상태 동기화 취약점 보강
- `SplitEditorScreen` 저장 시 기존 거래 수정 흐름에서
  - `expectedDate`
  - `isConfirmed`
  - `hasAlarm`
  값을 유지하도록 보정
- `LedgerScreen`에서 선택 월에 데이터가 사라진 경우 최신 데이터 월로 동기화

### 6) 테스트 추가
- 신규 테스트: `app/src/test/java/com/jaehwan/moneybook/transaction/TransactionPoliciesTest.kt`
  - 금액 파싱/포맷
  - split 완료/미정산 합계
  - 월 집계/현재 잔고 계산
  - 거래 입력 검증
  - 분배 합계 보존

## 변경 파일
- `app/src/main/java/com/jaehwan/moneybook/ui/root/MoneyBookApp.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/domain/MoneyUiPolicy.kt` (new)
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerTradePolicies.kt` (new)
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionEntryValidation.kt` (new)
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TradeScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionDetailScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionEntryScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionFormDialog.kt`
- `app/src/main/java/com/jaehwan/moneybook/splitmember/ui/SplitEditorScreen.kt`
- `app/src/test/java/com/jaehwan/moneybook/transaction/TransactionPoliciesTest.kt` (new)

## 확인 메모
- IDE 린트 기준 오류 없음
- 로컬 Java Runtime 미설치 환경에서는 Gradle 컴파일 확인 제한 가능
