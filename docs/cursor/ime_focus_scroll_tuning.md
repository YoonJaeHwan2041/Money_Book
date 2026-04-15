# IME 입력 가림 대응 가이드

키보드(IME)가 올라올 때 입력창이 가려지는 문제를 줄이기 위해 적용한 구조와, 실기기에서 미세 조정하는 방법을 정리한 문서입니다.

---

## 1) 적용한 구조

### A. Activity 레벨 resize

- 파일: `app/src/main/AndroidManifest.xml`
- 설정: `android:windowSoftInputMode="adjustResize"`
- 목적: IME가 올라올 때 루트 레이아웃 높이를 줄여 Compose가 가용 영역을 정확히 계산하도록 함.

### B. 스크롤 컨테이너 + IME 패딩

각 폼의 스크롤 컨테이너에 아래 순서로 적용:

1. `imePadding()`
2. `verticalScroll(...)`
3. `onGloballyPositioned { viewportCoords = it }`

적용 파일:

- `app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionFormDialog.kt`
- `app/src/main/java/com/jaehwan/moneybook/category/ui/CategoryFormDialog.kt`
- `app/src/main/java/com/jaehwan/moneybook/splitmember/ui/SplitEditorScreen.kt`

### C. 포커스 시 목표 위치로 스크롤

- 공통 Modifier 파일:
  `app/src/main/java/com/jaehwan/moneybook/ui/FocusScrollToVerticalBiasInViewport.kt`
- 핵심 아이디어:
  - 포커스된 입력 필드 좌표(`boundsInRoot`) 측정
  - 스크롤 뷰포트 좌표(`boundsInRoot`) 측정
  - 필드 중심이 뷰포트의 `verticalBias` 위치(예: 0.40)로 오도록 `ScrollState.scrollTo(...)` 수행
  - IME 애니메이션 타이밍 오차를 대비해 여러 번 재시도

---

## 2) 미세 조정 포인트 (가장 자주 수정)

파일: `app/src/main/java/com/jaehwan/moneybook/ui/FocusScrollToVerticalBiasInViewport.kt`

### `DEFAULT_VERTICAL_BIAS`

- 기본값: `0.40f`
- 의미:
  - `0.5f` = 뷰포트 정중앙
  - 값을 낮출수록 입력창이 더 위로 감 (`0.35f` 추천 시작점)

### `SCROLL_RETRY_DELAYS_MS`

- 기본값: `listOf(0L, 90L, 150L, 220L)`
- 의미: 포커스 후 스크롤 보정 재시도 시점(ms)
- 맨 아래 입력이 계속 가려지면:
  - 마지막 값을 키움: `... 280L`, `... 320L`
  - 재시도 단계 추가: `listOf(0L, 90L, 150L, 240L, 320L)`

---

## 3) 화면별 커스텀 방법

기본은 공통값을 쓰고, 특정 화면만 다르게 하고 싶다면
`focusScrollToVerticalBiasInViewport(...)` 호출 시 `verticalBias`를 직접 전달하면 됩니다.

예시:

```kotlin
.focusScrollToVerticalBiasInViewport(
    scrollState = formScrollState,
    viewportCoordinates = { formViewportCoords },
    coroutineScope = formScrollScope,
    verticalBias = 0.35f,
)
```

---

## 4) 권장 튜닝 순서

1. `DEFAULT_VERTICAL_BIAS`를 `0.40f -> 0.35f`로 낮춤
2. 그래도 가리면 `SCROLL_RETRY_DELAYS_MS` 마지막 값을 `220L -> 280L/320L`로 증가
3. 특정 화면만 문제면 해당 입력에 `verticalBias` 인자를 별도로 전달

---

## 5) 체크 시나리오

1. 거래 수정 다이얼로그에서 하단 입력 포커스
2. 뿜빠이 수정에서 가장 아래 멤버 이름/금액 입력 포커스
3. 멤버 메모 다이얼로그 입력 포커스
4. 키보드 표시 직후 손으로 스크롤하지 않아도 입력칸이 보이는지 확인

