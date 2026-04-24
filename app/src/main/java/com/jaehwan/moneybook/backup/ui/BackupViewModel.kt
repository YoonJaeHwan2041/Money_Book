package com.jaehwan.moneybook.backup.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaehwan.moneybook.backup.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null, isError = false)
            runCatching { backupRepository.exportTo(uri) }
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "백업 파일이 저장되었습니다.",
                        isError = false,
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = error.message ?: "백업 저장에 실패했습니다.",
                        isError = true,
                    )
                }
        }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = null, isError = false)
            backupRepository.importFrom(uri)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "백업 복원이 완료되었습니다.",
                        isError = false,
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = error.message ?: "백업 복원에 실패했습니다.",
                        isError = true,
                    )
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)
