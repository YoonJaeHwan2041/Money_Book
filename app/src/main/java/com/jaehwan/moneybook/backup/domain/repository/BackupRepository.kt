package com.jaehwan.moneybook.backup.domain.repository

import android.net.Uri

interface BackupRepository {
    suspend fun exportTo(uri: Uri)
    suspend fun validate(uri: Uri): Result<Unit>
    suspend fun importFrom(uri: Uri): Result<Unit>
}
