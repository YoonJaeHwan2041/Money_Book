# 가계부(거래) 기능 구현 기록

UI는 이후에 다듬을 예정이며, 본 문서는 **기능·데이터·구조** 위주로 정리합니다.

---

## 개요

- **가계부 탭**: Drawer의「가계부」에서 거래 목록·추가·수정·삭제.
- **거래 종류** (`TransactionEntity.type` 문자열):  
  `INCOME`, `EXPENSE`, `FIXED_INCOME`, `FIXED_EXPENSE`, `SPLIT`
- **SPLIT**: 현재는 **type만 저장**. `SplitMemberEntity`·멤버 UI는 추후.
- **메모**: `TransactionEntity.memo`를 **`String?`(nullable)** 로 변경.

---

## 데이터베이스·DAO

### `AppDatabase`

- **version 1 → 2** (`memo` nullable 등 스키마 변경).
- 개발용으로 `DatabaseModule`에 **`fallbackToDestructiveMigration()`** 이 있으면, 버전 올릴 때 기존 로컬 DB가 재생성될 수 있음.

### `TransactionDao`

- 기간 조회: `FROM 'transaction'` → **`FROM \`transaction\``** (백틱) 수정.
- `insertTransaction` → **`suspend fun insertTransaction(...): Long`** (생성 id 반환).
- 메서드명 정리: `updateTransaction`, `deleteTransaction`.

### `SplitMemberDao` (정산 착수 전 정리)

- `transaction_id` 조회 인자 **`Long`**, **`suspend`** 반환.
- `insertMembers` 등 이름 정리. (아직 앱 코드에서 SPLIT 멤버 insert는 사용하지 않음.)

---

## 도메인·DI

| 항목 | 경로 |
|------|------|
| 거래 종류 상수 | [`transaction/domain/model/TransactionType.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/domain/model/TransactionType.kt) |
| Repository 포트 | [`transaction/domain/repository/TransactionRepository.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/domain/repository/TransactionRepository.kt) |
| 구현체 | [`transaction/data/local/TransactionRepositoryImpl.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/data/local/TransactionRepositoryImpl.kt) |
| Hilt `@Binds` | [`di/TransactionRepositoryModule.kt`](../../app/src/main/java/com/jaehwan/moneybook/di/TransactionRepositoryModule.kt) |

`LedgerViewModel`은 `TransactionRepository`와 **`CategoryRepository`**를 `combine`하여 목록에 **카테고리명**을 붙입니다.

---

## 고정 수입·고정 지출 동작

- **`isConfirmed`**: 실제 수입·지출이 반영됐는지 **사용자가 직접 토글**.
- **`expectedDate`**: 예정일. 폼에서 **DatePickerDialog**로 변경 가능.
- **`hasAlarm`**: 고정 종류에서만 폼에 노출·선택. (실제 OS 알림은 미구현, 추후 `hasAlarm` + 미확정 조건 등으로 확장 가능.)
- **일반 수입·지출·SPLIT**: 확정·알람·예정일은 폼 로직상 단순화(고정이 아니면 확정 처리, 알람 끔, 예정일은 당일 시작 등).

---

## UI 파일 (기능 중심)

| 파일 | 역할 |
|------|------|
| [`LedgerViewModel.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerViewModel.kt) | 목록 상태, insert/update/delete |
| [`LedgerRow.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerRow.kt) | 거래 + 카테고리명 한 줄 모델 |
| [`LedgerScreen.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerScreen.kt) | 목록, 고정+미확정 시 시각적 구분, 수정/삭제 |
| [`TransactionFormDialog.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/ui/TransactionFormDialog.kt) | 종류·금액·카테고리·메모·고정 전용 필드 |
| [`LedgerDateUtils.kt`](../../app/src/main/java/com/jaehwan/moneybook/transaction/ui/LedgerDateUtils.kt) | `startOfDayMillis()` |

### 앱 루트 연동

- [`MoneyBookApp.kt`](../../app/src/main/java/com/jaehwan/moneybook/ui/root/MoneyBookApp.kt): 가계부 탭에 `LedgerScreen`, **거래 추가 FAB**, 거래 추가/수정/삭제 다이얼로그.
- 기존 `LedgerPlaceholderScreen`은 **제거**됨.

---

## Compose 이슈 (참고)

- **`ExposedDropdownMenu`**: `ExposedDropdownMenuBox` **스코프 안**에서만 쓰는 확장이라 `import androidx.compose.material3.ExposedDropdownMenu` 단독 import는 실패함 → import 제거.
- **`menuAnchor()`**: 현재 프로젝트의 Compose BOM(`2024.10.01`) 조합에서는 **미제공**이라 `OutlinedTextField`에 `fillMaxWidth()`만 사용.

---

## 아직 하지 않은 것

- 거래·가계부 **UI 폴리시** (레이아웃, 타이포, 접근성 등).
- SPLIT **멤버·정산 금액** 로직.
- **`hasAlarm` 기반 실제 알림** (스케줄/푸시).
- 월별 요약·필터 등.

---

## 요약

가계부 탭에 **거래 CRUD**와 **5종류 type**, **고정 거래의 확정·알람·예정일**, **nullable 메모**를 반영했고, **SPLIT은 type만** 저장합니다. UI는 추후 개선하고, 본 문서는 구현·스키마·파일 위치를 추적하는 용도로 쓰면 됩니다.
