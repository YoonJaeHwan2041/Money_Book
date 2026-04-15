# 뿜빠이(SPLIT) 기능 구현 정리

본 문서는 MoneyBook의 뿜빠이 기능을 **데이터 스키마 / 저장 흐름 / UI 흐름 / 튜닝 포인트** 기준으로 정리한 문서입니다.

---

## 1. 구현 범위

- 거래 타입에 `SPLIT`(라벨: `뿜빠이`) 추가
- 뿜빠이 전용 멤버 엔티티 확장 (`SplitMemberEntity`)
- 뿜빠이 전용 입력 화면 (`SplitEditorScreen`)
- 거래 목록에서 뿜빠이 정산 상태(수금 진행) 및 멤버별 수금 토글
- 저장 시 일반 거래 저장과 분리된 `insertSplit` / `updateSplit` 경로 사용

---

## 2. 주요 파일

- 타입: `app/src/main/java/com/jaehwan/moneybook/transaction/domain/model/TransactionType.kt`
- 멤버 엔티티: `app/src/main/java/com/jaehwan/moneybook/splitmember/data/local/SplitMemberEntity.kt`
- DAO: `app/src/main/java/com/jaehwan/moneybook/splitmember/data/local/SplitMemberDao.kt`
- 분배 계산기: `app/src/main/java/com/jaehwan/moneybook/splitmember/domain/SplitAmountCalculator.kt`
- 저장소 인터페이스/구현:
  - `app/src/main/java/com/jaehwan/moneybook/transaction/domain/repository/TransactionRepository.kt`
  - `app/src/main/java/com/jaehwan/moneybook/transaction/data/local/TransactionRepositoryImpl.kt`
- 뷰모델: `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerViewModel.kt`
- 뿜빠이 에디터: `app/src/main/java/com/jaehwan/moneybook/splitmember/ui/SplitEditorScreen.kt`
- 거래 폼 연동: `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionFormDialog.kt`
- 앱 루트 연동: `app/src/main/java/com/jaehwan/moneybook/ui/root/MoneyBookApp.kt`
- 목록 표시: `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerScreen.kt`

---

## 3. 데이터 모델 / DB

### 3.1 `SplitMemberEntity` 확장 필드

- `is_primary_payer`: 총액 결제자 여부
- `extra_amount`: 개인 추가 부담
- `deduction_amount`: 개인 차감
- `agreed_amount`: 확정 정산 금액
- `is_paid`: 수금 여부
- `payment_memo`: 멤버 메모
- `created_at`, `updated_at`

### 3.2 정렬 규칙

`SplitMemberDao` 조회 시 결제자를 항상 상단에 두도록 정렬:

- `ORDER BY is_primary_payer DESC, id ASC`

### 3.3 DB 버전

- `AppDatabase` 버전이 뿜빠이 관련 스키마 반영으로 증가됨.

---

## 4. 저장 흐름

### 4.1 신규 생성

1. `TransactionFormDialog`에서 타입을 `SPLIT`으로 선택
2. 확인 버튼이 `뿜빠이 상세 입력`으로 동작
3. 금액/카테고리/메모를 프리필로 `SplitEditorScreen` 오픈
4. 저장 시 `LedgerViewModel.insertSplit(tx, members)`
5. `TransactionRepositoryImpl.insertSplit`에서 트랜잭션 + 멤버 일괄 저장

### 4.2 수정

1. 거래 목록에서 SPLIT 거래 수정
2. `SplitEditorScreen`으로 진입
3. 기존 멤버 로드 후 편집
4. 저장 시 `LedgerViewModel.updateSplit(tx, members)`
5. 저장소에서 기존 멤버 삭제 후 최신 목록으로 재삽입

### 4.3 멤버 수금 토글

목록 카드 펼침에서 멤버별 `Switch` 조작 시:

- `LedgerViewModel.updateSplitMember(member)` 호출
- `is_paid` 및 `updated_at` 반영

---

## 5. UI 동작

### 5.1 `TransactionFormDialog`

- SPLIT 선택 시 일반 저장 버튼 대신 `뿜빠이 상세 입력` 분기
- 일반 거래(INCOME/EXPENSE/FIXED)는 기존 저장 경로 유지

### 5.2 `SplitEditorScreen`

- 총액 고정 표시
- 멤버 수 조절 (신규)
- 멤버별 이름/추가/차감/제안금액/확정금액/메모
- 제안 금액 사용 체크
- 저장 시 `TransactionEntity + List<SplitMemberEntity>` 전달
- 수정 모드에서 기존 `is_paid` 상태 유지

### 5.3 `LedgerScreen`

- SPLIT 카드에서 `수금 x / n` 표시
- `정산 펼치기/접기`
- 멤버별 정산 금액 및 수금 토글

---

## 6. 정산 금액 계산 규칙

`computeSuggestedShares(...)` 기준:

1. 기본 균등 분배
2. 멤버별 `extra_amount` 더함
3. 멤버별 `deduction_amount` 뺌
4. 반올림/잔여 차이를 결제자(기본 인덱스 0)에게 보정

---

## 7. 키보드(IME) 관련 보완

뿜빠이 입력 화면 포함 폼 화면 전반에 다음 적용:

- `windowSoftInputMode="adjustResize"` (Manifest)
- `imePadding()` + `verticalScroll(...)`
- 포커스 입력 자동 스크롤 공통 Modifier:
  `app/src/main/java/com/jaehwan/moneybook/ui/FocusScrollToVerticalBiasInViewport.kt`

세부 튜닝 문서:

- `docs/cursor/ime_focus_scroll_tuning.md`

---

## 8. 체크리스트

- [ ] SPLIT 신규 생성 후 멤버 저장 확인
- [ ] SPLIT 수정 후 멤버/금액 반영 확인
- [ ] 목록에서 수금 토글 후 상태 유지 확인
- [ ] 화면 하단 입력 포커스 시 자동 스크롤 확인

