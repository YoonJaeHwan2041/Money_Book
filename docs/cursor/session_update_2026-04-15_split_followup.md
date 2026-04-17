# 2026-04-15 추가 작업 요약 (Split 후속)

## 작업 목적
- 거래 추가/상세/목록에서 Split(뿜빠이) UX를 실제 사용 흐름에 맞게 안정화
- 키보드 가림, 토글 반응성, 중복 진입 버튼, 잔고 표기 방식 이슈 정리

## 반영 내용

### 1) 거래 추가 화면 (`TransactionEntryScreen`)
- Split 섹션 입력 흐름 보강
  - 자동 `1` 강제 입력 제거
  - 저장 시 빈칸 검증 메시지 표시(금액/인원수/본인 부담금/멤버명/멤버금액)
- 키보드 대응
  - 포커스 스크롤 보정 유지
  - 하단 `WindowInsets.ime` 기반 여유 공간 추가로 마지막 입력칸 가림 완화
- 참고값 정책 유지
  - 자동 계산값은 텍스트 참고용으로만 표시
  - 멤버 금액은 직접 입력 또는 일괄 적용으로만 반영

### 2) 뿜빠이 상세/드롭다운 반응성
- 상세 화면 데이터 갱신 방식 개선 (`MoneyBookApp`)
  - 상세 선택 상태를 `LedgerRow` 객체 보관에서 `transactionId` 보관 방식으로 변경
  - DB 갱신 후 최신 `ledgerRows`를 다시 조회해 UI 즉시 반영
- 토글 클릭 충돌 완화 (`TradeScreen`)
  - 카드 전체 클릭 대신 상단 행 클릭만 상세 진입 처리
  - 드롭다운 내부 `Switch`가 상세 이동 클릭에 먹히지 않도록 조정

### 3) 미정산 금액 합계 표시
- `TradeScreen` 드롭다운에 `미정산 금액 합계` 추가
- `TransactionDetailScreen` 뿜빠이 요약 카드에 `미정산 금액 합계` 추가
- 계산 기준: 본인(결제자) 제외 + 미정산 멤버의 합의금 합계

### 4) 본인 행 토글 제거
- 아래 3개 화면에서 결제자(`isPrimaryPayer`)는 토글 제거, 텍스트만 표시
  - `TransactionDetailScreen`
  - `TradeScreen` 드롭다운
  - `LedgerScreen` 인라인 정산
- 표시 문구: `본인 부담 완료`

### 5) FAB/잔고 표기 정리
- `MoneyBookApp`
  - 홈/거래 공통 하단 FAB 제거
  - 거래 탭 상단 `+`만 사용
- `LedgerScreen`
  - 카드 제목 `이번 달 잔액` -> `현재 통장 잔고`
  - 잔고 계산을 월 합계가 아닌 전체 누적 기준으로 분리
  - `총 수입/총 지출`은 기존 월 기준 유지

## 변경 파일
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionEntryScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TradeScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionDetailScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/ui/root/MoneyBookApp.kt`
- `docs/cursor/session_update_2026-04-15_split_followup.md`

## 확인 메모
- IDE `ReadLints` 기준 린트 에러 없음
- 로컬 환경에서 Java Runtime 부재로 Gradle 컴파일은 미실행 상태
