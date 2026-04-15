# 카테고리 드롭다운 이슈 리포트 (2026-04-15)

본 문서는 거래 추가 다이얼로그의 카테고리 드롭다운이 열리지 않던 이슈에 대한 원인 추적과 시도 내역을 기록합니다.

---

## 1) 증상

- 거래 추가 다이얼로그에서 카테고리 영역을 탭해도 드롭다운이 열리지 않음
- 카테고리 데이터는 존재하는 상태

대상 화면:

- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionFormDialog.kt`

---

## 2) 확인 로그

디버그 태그:

- `TxFormDropdown`

핵심 관찰:

- `render category field ... expanded=false` 로그는 출력됨
- `render dropdown visible=false` 로그는 출력됨
- `categoryExpanded changed=false` 로그는 출력됨
- **필드 클릭 로그(`category field clicked before/after`)는 출력되지 않음**

해석:

- 탭 이벤트는 화면(`ViewPostIme`)에는 도달하지만, 카테고리 필드의 클릭 핸들러까지 전달되지 않는 케이스
- 즉, 상태 토글 코드 자체가 실행되지 않는 구간이 있음

---

## 3) 시도한 방법

1. 필드 위 클릭 오버레이(`Box` overlay) 적용  
2. 드롭다운 팝업 대신 카테고리 선택 다이얼로그(AlertDialog) 방식으로 우회  
3. 인라인 드롭다운(`AnimatedVisibility + Card list`) 방식 적용  
4. 필드 클릭 제거 + trailing icon(`▼`) 버튼으로만 토글하도록 변경  

---

## 4) 원인 가설

기기/버전/다이얼로그 조합에서 아래 요인이 겹쳐 클릭 전달이 불안정했을 가능성:

- `AlertDialog` 내부 터치 우선순위
- `OutlinedTextField`의 내부 포인터 소비
- `readOnly` 필드 + 외부 `clickable` 조합의 기기별 차이
- 스크롤/포커스 처리(`imePadding`, `verticalScroll`)와의 상호작용

---

## 5) 현재 코드 상태(기록 시점)

- 카테고리 필드에 드롭다운 토글이 붙어 있음
- 디버깅 로그가 삽입되어 상태/렌더 추적 가능

참고 파일:

- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionFormDialog.kt`

---

## 6) 권장 안정화 방향

드롭다운 UI를 유지하면서 안정성을 높이려면:

1. 토글 트리거를 필드 본문이 아닌 **명시적 버튼(IconButton)**으로 고정  
2. 인라인 리스트는 유지하되, 클릭 타깃 영역을 충분히 크게 확보  
3. 필요 시 `Popup`/`DropdownMenu` 대신 `ModalBottomSheet`를 선택(기기 편차 최소화)

---

## 7) 로그 재현 방법

1. 앱 실행 → 거래 추가 다이얼로그 진입  
2. 카테고리 영역 탭  
3. Logcat에서 `TxFormDropdown` 필터로 확인  

확인할 로그:

- 클릭 전/후 토글 로그 존재 여부
- `categoryExpanded changed` 값 전환 여부
- `dropdown content composing` 출력 여부

