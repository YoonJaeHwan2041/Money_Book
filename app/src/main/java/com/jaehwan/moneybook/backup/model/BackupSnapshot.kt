package com.jaehwan.moneybook.backup.model

import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.fixed.data.local.FixedScheduleEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentPaymentEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentPlanEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupSnapshot(
    val schemaVersion: Int,
    val exportedAt: Long,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val splitMembers: List<SplitMemberEntity>,
    val installmentPlans: List<InstallmentPlanEntity>,
    val installmentPayments: List<InstallmentPaymentEntity>,
    val fixedSchedules: List<FixedScheduleEntity>,
) {
    fun toJsonString(): String {
        val root = JSONObject()
            .put("schemaVersion", schemaVersion)
            .put("exportedAt", exportedAt)
            .put("categories", JSONArray(categories.map { it.toJson() }))
            .put("transactions", JSONArray(transactions.map { it.toJson() }))
            .put("splitMembers", JSONArray(splitMembers.map { it.toJson() }))
            .put("installmentPlans", JSONArray(installmentPlans.map { it.toJson() }))
            .put("installmentPayments", JSONArray(installmentPayments.map { it.toJson() }))
            .put("fixedSchedules", JSONArray(fixedSchedules.map { it.toJson() }))
        return root.toString()
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1

        fun fromJsonString(json: String): BackupSnapshot {
            val root = JSONObject(json)
            return BackupSnapshot(
                schemaVersion = root.optInt("schemaVersion", -1),
                exportedAt = root.optLong("exportedAt", 0L),
                categories = root.optJSONArray("categories").toCategoryList(),
                transactions = root.optJSONArray("transactions").toTransactionList(),
                splitMembers = root.optJSONArray("splitMembers").toSplitMemberList(),
                installmentPlans = root.optJSONArray("installmentPlans").toInstallmentPlanList(),
                installmentPayments = root.optJSONArray("installmentPayments").toInstallmentPaymentList(),
                fixedSchedules = root.optJSONArray("fixedSchedules").toFixedScheduleList(),
            )
        }
    }
}

private fun CategoryEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("iconKey", iconKey)
    .put("isDefault", isDefault)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)

private fun TransactionEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("categoryId", categoryId)
    .put("amount", amount)
    .put("type", type)
    .put("isConfirmed", isConfirmed)
    .put("expectedDate", expectedDate)
    .put("hasAlarm", hasAlarm)
    .put("memo", memo)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)

private fun SplitMemberEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("transactionId", transactionId)
    .put("memberName", memberName)
    .put("isPrimaryPayer", isPrimaryPayer)
    .put("extraAmount", extraAmount)
    .put("deductionAmount", deductionAmount)
    .put("agreedAmount", agreedAmount)
    .put("isPaid", isPaid)
    .put("paymentMemo", paymentMemo)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)

private fun InstallmentPlanEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("transactionId", transactionId)
    .put("totalAmount", totalAmount)
    .put("months", months)
    .put("startDate", startDate)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)

private fun InstallmentPaymentEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("planId", planId)
    .put("sequenceNo", sequenceNo)
    .put("dueDate", dueDate)
    .put("amount", amount)
    .put("isPaid", isPaid)
    .put("paidAt", paidAt)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)

private fun FixedScheduleEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("kind", kind)
    .put("categoryId", categoryId)
    .put("amount", amount)
    .put("memo", memo)
    .put("dayOfMonth", dayOfMonth)
    .put("triggerHour", triggerHour)
    .put("isActive", isActive)
    .put("startYearMonth", startYearMonth)
    .put("endYearMonth", endYearMonth)
    .put("lastGeneratedYearMonth", lastGeneratedYearMonth)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)

private fun JSONArray?.toCategoryList(): List<CategoryEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        CategoryEntity(
            id = item.getLong("id"),
            name = item.getString("name"),
            iconKey = item.optionalString("iconKey"),
            isDefault = item.optBoolean("isDefault", false),
            createdAt = item.optLong("createdAt", 0L),
            updatedAt = item.optLong("updatedAt", 0L),
        )
    }
}

private fun JSONArray?.toTransactionList(): List<TransactionEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        TransactionEntity(
            id = item.getLong("id"),
            categoryId = item.getLong("categoryId"),
            amount = item.getInt("amount"),
            type = item.getString("type"),
            isConfirmed = item.optBoolean("isConfirmed", false),
            expectedDate = item.getLong("expectedDate"),
            hasAlarm = item.optBoolean("hasAlarm", false),
            memo = item.optionalString("memo"),
            createdAt = item.optLong("createdAt", 0L),
            updatedAt = item.optLong("updatedAt", 0L),
        )
    }
}

private fun JSONArray?.toSplitMemberList(): List<SplitMemberEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        SplitMemberEntity(
            id = item.getLong("id"),
            transactionId = item.getLong("transactionId"),
            memberName = item.getString("memberName"),
            isPrimaryPayer = item.optBoolean("isPrimaryPayer", false),
            extraAmount = item.optInt("extraAmount", 0),
            deductionAmount = item.optInt("deductionAmount", 0),
            agreedAmount = if (item.isNull("agreedAmount")) null else item.getInt("agreedAmount"),
            isPaid = item.optBoolean("isPaid", false),
            paymentMemo = item.optionalString("paymentMemo"),
            createdAt = item.optLong("createdAt", 0L),
            updatedAt = item.optLong("updatedAt", 0L),
        )
    }
}

private fun JSONArray?.toInstallmentPlanList(): List<InstallmentPlanEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        InstallmentPlanEntity(
            id = item.getLong("id"),
            transactionId = item.getLong("transactionId"),
            totalAmount = item.getInt("totalAmount"),
            months = item.getInt("months"),
            startDate = item.getLong("startDate"),
            createdAt = item.optLong("createdAt", 0L),
            updatedAt = item.optLong("updatedAt", 0L),
        )
    }
}

private fun JSONArray?.toInstallmentPaymentList(): List<InstallmentPaymentEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        InstallmentPaymentEntity(
            id = item.getLong("id"),
            planId = item.getLong("planId"),
            sequenceNo = item.getInt("sequenceNo"),
            dueDate = item.getLong("dueDate"),
            amount = item.getInt("amount"),
            isPaid = item.optBoolean("isPaid", false),
            paidAt = if (item.isNull("paidAt")) null else item.getLong("paidAt"),
            createdAt = item.optLong("createdAt", 0L),
            updatedAt = item.optLong("updatedAt", 0L),
        )
    }
}

private fun JSONArray?.toFixedScheduleList(): List<FixedScheduleEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        FixedScheduleEntity(
            id = item.getLong("id"),
            kind = item.getString("kind"),
            categoryId = item.getLong("categoryId"),
            amount = item.getInt("amount"),
            memo = item.optionalString("memo"),
            dayOfMonth = item.optInt("dayOfMonth", 1),
            triggerHour = item.optInt("triggerHour", 14),
            isActive = item.optBoolean("isActive", true),
            startYearMonth = item.getString("startYearMonth"),
            endYearMonth = item.optionalString("endYearMonth"),
            lastGeneratedYearMonth = item.optionalString("lastGeneratedYearMonth"),
            createdAt = item.optLong("createdAt", 0L),
            updatedAt = item.optLong("updatedAt", 0L),
        )
    }
}

private fun JSONObject.optionalString(key: String): String? {
    if (isNull(key)) return null
    val value = optString(key, "").trim()
    return value.takeIf { it.isNotEmpty() && it != "null" }
}
