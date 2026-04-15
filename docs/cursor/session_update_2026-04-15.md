# 작업 정리 (2026-04-15, 추가분)

본 문서는 이전 문서 작성 이후에 반영된 **추가 변경분만** 정리합니다.  
기존 SPLIT 상세 설계/구현 내역은 기존 문서를 따릅니다.

---

## 1) 앱 시작 UX 변경

- 앱 시작 시 1초 스플래시(`가계부`) 표시
- 스플래시 종료 후 기본 진입 탭을 `카테고리`가 아닌 `가계부(Ledger)`로 변경

적용 파일:

- `app/src/main/java/com/jaehwan/moneybook/ui/root/MoneyBookApp.kt`

---

## 2) 가계부 홈형 UI 개편

- Ledger 화면을 홈 대시보드 형태로 재구성
  - 상단 인사/월 표시
  - 월간 요약 카드(잔액/수입/지출)
  - 카테고리별 지출 섹션
  - 최근 거래 리스트
- 기존 거래 수정/삭제 및 SPLIT 카드 동작은 유지

적용 파일:

- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerScreen.kt`

---

## 3) 금액 포맷(천단위 콤마) 추가 적용

- 거래 입력 폼 금액 입력에 콤마 포맷 반영
- 저장 시 콤마 제거 후 숫자 파싱
- SPLIT 편집 화면(추가/차감/확정/제안/총액) 표시 및 입력 포맷 반영
- Ledger 표시 금액 콤마 통일

적용 파일:

- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionFormDialog.kt`
- `app/src/main/java/com/jaehwan/moneybook/splitmember/ui/SplitEditorScreen.kt`
- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerScreen.kt`

---

## 4) 기본 카테고리 시드 확장

- 기존 자동 시드 정책 유지(카테고리 비어 있을 때만 생성)
- `월급` 카테고리 추가: `res:give_money`

적용 파일:

- `app/src/main/java/com/jaehwan/moneybook/category/data/local/CategoryRepositoryImpl.kt`

---

## 5) 카테고리 아이콘 렌더링 보정

- `res:<name>` 키가 텍스트 fallback이 아닌 실제 drawable 리소스를 렌더하도록 보정

적용 파일:

- `app/src/main/java/com/jaehwan/moneybook/category/ui/CategoryIconDisplay.kt`

---

## 6) 거래 금액 색상 규칙

- 최근 거래 카드 금액 색상/부호 적용
  - 수입(`INCOME`, `FIXED_INCOME`) → 초록, `+`
  - 지출(`EXPENSE`, `SPLIT`, `FIXED_EXPENSE`) → 빨강, `-`

적용 파일:

- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerScreen.kt`

---

## 7) 검증

- 주요 변경 후 `./gradlew :app:compileDebugKotlin`로 컴파일 확인
- 작성 시점 기준 빌드 성공
