package com.jaehwan.moneybook.backup.data.local

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.jaehwan.moneybook.backup.domain.repository.BackupRepository
import com.jaehwan.moneybook.backup.model.BackupSnapshot
import com.jaehwan.moneybook.common.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
) : BackupRepository {

    override suspend fun exportTo(uri: Uri) {
        val snapshot = db.withTransaction {
            BackupSnapshot(
                schemaVersion = BackupSnapshot.CURRENT_SCHEMA_VERSION,
                exportedAt = System.currentTimeMillis(),
                categories = db.categoryDao().getAllCategoriesOnce(),
                transactions = db.transactionDao().getAllTransactionsOnce(),
                splitMembers = db.splitMemberDao().getAllMembersOnce(),
                installmentPlans = db.installmentDao().getAllPlansOnce(),
                installmentPayments = db.installmentDao().getAllPaymentsOnce(),
                fixedSchedules = db.fixedScheduleDao().getAllSchedulesOnce(),
            )
        }

        val payload = snapshot.toJsonString()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(payload.toByteArray(StandardCharsets.UTF_8))
            stream.flush()
        } ?: error("백업 파일을 열 수 없습니다.")
    }

    override suspend fun validate(uri: Uri): Result<Unit> = runCatching {
        val snapshot = readSnapshot(uri)
        validateSnapshot(snapshot)
    }

    override suspend fun importFrom(uri: Uri): Result<Unit> = runCatching {
        val snapshot = readSnapshot(uri)
        validateSnapshot(snapshot)

        db.withTransaction {
            // Child -> Parent 순서로 정리
            db.installmentDao().deleteAllPayments()
            db.installmentDao().deleteAllPlans()
            db.splitMemberDao().deleteAllMembers()
            db.fixedScheduleDao().deleteAllSchedules()
            db.transactionDao().deleteAllTransactions()
            db.categoryDao().deleteAllCategories()

            db.categoryDao().upsertCategories(snapshot.categories)
            db.transactionDao().upsertTransactions(snapshot.transactions)
            db.splitMemberDao().insertMembers(snapshot.splitMembers)
            db.installmentDao().insertPlans(snapshot.installmentPlans)
            db.installmentDao().insertPayments(snapshot.installmentPayments)
            db.fixedScheduleDao().upsertSchedules(snapshot.fixedSchedules)
        }
    }

    private fun readSnapshot(uri: Uri): BackupSnapshot {
        val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(StandardCharsets.UTF_8)
        } ?: error("복원 파일을 읽을 수 없습니다.")
        return BackupSnapshot.fromJsonString(raw)
    }

    private fun validateSnapshot(snapshot: BackupSnapshot) {
        require(snapshot.schemaVersion == BackupSnapshot.CURRENT_SCHEMA_VERSION) {
            "지원하지 않는 백업 버전입니다. (파일=${snapshot.schemaVersion}, 지원=${BackupSnapshot.CURRENT_SCHEMA_VERSION})"
        }
    }
}
