package com.jaehwan.moneybook.transaction.ui

data class SplitDraftValidationInput(
    val participantCountText: String,
    val selfAmountText: String,
    val memberDrafts: List<SplitMemberDraftInput>,
)

data class SplitMemberDraftInput(
    val name: String,
    val amountText: String,
)

fun validateTransactionAmount(amountText: String): String? {
    if (amountText.isBlank()) return "금액 칸은 빈칸입니다."
    return null
}

fun validateSplitInput(input: SplitDraftValidationInput): String? {
    if (input.participantCountText.isBlank()) return "총 인원 수 칸은 빈칸입니다."
    if (input.selfAmountText.isBlank()) return "본인 부담금 칸은 빈칸입니다."
    input.memberDrafts.forEachIndexed { index, draft ->
        if (draft.name.isBlank()) return "멤버 ${index + 1} 이름 칸은 빈칸입니다."
        if (draft.amountText.isBlank()) return "멤버 ${index + 1} 금액 칸은 빈칸입니다."
    }
    return null
}
