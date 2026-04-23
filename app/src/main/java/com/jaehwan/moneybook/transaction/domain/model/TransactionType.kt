package com.jaehwan.moneybook.transaction.domain.model

enum class TransactionType(val key: String, val label: String) {
    INCOME("INCOME", "수입"),
    EXPENSE("EXPENSE", "지출"),
    INSTALLMENT("INSTALLMENT", "할부"),
    FIXED_INCOME("FIXED_INCOME", "고정 수입"),
    FIXED_EXPENSE("FIXED_EXPENSE", "고정 지출"),
    SPLIT("SPLIT", "뿜빠이"),
    ;

    val isFixed: Boolean
        get() = this == FIXED_INCOME || this == FIXED_EXPENSE

    companion object {
        fun fromKey(key: String): TransactionType =
            entries.find { it.key == key } ?: EXPENSE
    }
}
