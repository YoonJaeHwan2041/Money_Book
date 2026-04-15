package com.jaehwan.moneybook.category.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.ui.focusScrollToVerticalBiasInViewport

@Composable
fun CategoryFormDialog(
    initialCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconKey: String?) -> Unit,
) {
    val formKey = initialCategory?.id ?: 0L
    val iconInitial = remember(formKey) { iconKeyToFormInitial(initialCategory?.iconKey) }

    var name by remember(formKey) { mutableStateOf(initialCategory?.name.orEmpty()) }
    var selectedType by remember(formKey) { mutableStateOf(iconInitial.type) }
    var emojiText by remember(formKey) { mutableStateOf(iconInitial.emoji) }
    var selectedImageUri by remember(formKey) { mutableStateOf(iconInitial.galleryUri) }
    var selectedRes by remember(formKey) { mutableStateOf(iconInitial.resourceKey) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )
    val resOptions = listOf("ic_food", "ic_transport", "ic_shopping", "ic_health")

    val isEdit = initialCategory != null
    val dialogTitle = if (isEdit) "카테고리 수정" else "새 카테고리 추가"
    val confirmLabel = if (isEdit) "저장" else "추가"
    val formScrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            val maxBody = LocalConfiguration.current.screenHeightDp.dp * 0.88f
            var formViewportCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
            val formScrollScope = rememberCoroutineScope()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxBody)
                    .imePadding()
                    .verticalScroll(formScrollState)
                    .onGloballyPositioned { formViewportCoords = it }
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("카테고리 이름") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusScrollToVerticalBiasInViewport(
                            scrollState = formScrollState,
                            viewportCoordinates = { formViewportCoords },
                            coroutineScope = formScrollScope,
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("아이콘 설정", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconSelectionType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedType) {
                    IconSelectionType.EMOJI -> {
                        OutlinedTextField(
                            value = emojiText,
                            onValueChange = { if (it.length <= 2) emojiText = it },
                            label = { Text("이모지 1개 입력") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusScrollToVerticalBiasInViewport(
                                    scrollState = formScrollState,
                                    viewportCoordinates = { formViewportCoords },
                                    coroutineScope = formScrollScope,
                                )
                        )
                    }
                    IconSelectionType.GALLERY -> {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedImageUri == null) "갤러리에서 사진 선택" else "사진 변경")
                        }
                        if (selectedImageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .align(Alignment.CenterHorizontally),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    IconSelectionType.RESOURCE -> {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            resOptions.forEach { resName ->
                                val isSelected = selectedRes == resName
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                                        )
                                        .clickable { selectedRes = resName },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = resName.substringAfter("ic_").take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                    IconSelectionType.NONE -> {
                        Text("아이콘 없이 저장합니다.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val iconKey = when (selectedType) {
                        IconSelectionType.EMOJI ->
                            if (emojiText.isNotBlank()) "emoji:$emojiText" else null
                        IconSelectionType.GALLERY ->
                            selectedImageUri?.let { "uri:$it" }
                        IconSelectionType.RESOURCE -> "res:$selectedRes"
                        IconSelectionType.NONE -> null
                    }
                    if (name.isNotBlank()) {
                        onConfirm(name, iconKey)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
