package com.jaehwan.moneybook

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.category.ui.CategoryViewModel
import com.jaehwan.moneybook.ui.theme.MoneyBookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyBookTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CategoryViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Money Book") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CategoryList(categories = categories)
        }

        if (showDialog) {
            CategoryAddDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, iconKey ->
                    viewModel.addCategory(name, iconKey)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CategoryAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconKey: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(IconSelectionType.NONE) }
    
    // For Emoji
    var emojiText by remember { mutableStateOf("") }
    // For Gallery
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )
    // For Resource
    var selectedRes by remember { mutableStateOf("ic_food") }
    val resOptions = listOf("ic_food", "ic_transport", "ic_shopping", "ic_health")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 카테고리 추가") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("카테고리 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("아이콘 설정", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // Type Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconSelectionType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Input based on Type
                when (selectedType) {
                    IconSelectionType.EMOJI -> {
                        OutlinedTextField(
                            value = emojiText,
                            onValueChange = { if (it.length <= 2) emojiText = it },
                            label = { Text("이모지 1개 입력") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
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
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable { selectedRes = resName },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Placeholder for resource icon
                                    Text(
                                        text = resName.substringAfter("ic_").take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    IconSelectionType.NONE -> {
                        Text("아이콘 없이 생성합니다.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val iconKey = when (selectedType) {
                        IconSelectionType.EMOJI -> if (emojiText.isNotBlank()) "emoji:$emojiText" else null
                        IconSelectionType.GALLERY -> selectedImageUri?.let { "uri:$it" }
                        IconSelectionType.RESOURCE -> "res:$selectedRes"
                        IconSelectionType.NONE -> null
                    }
                    if (name.isNotBlank()) {
                        onConfirm(name, iconKey)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

enum class IconSelectionType(val title: String) {
    EMOJI("이모지"),
    GALLERY("갤러리"),
    RESOURCE("기본"),
    NONE("없음")
}

@Composable
fun CategoryList(categories: List<CategoryEntity>) {
    if (categories.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "등록된 카테고리가 없습니다.", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "+ 버튼을 눌러 추가해보세요!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = categories,
                key = { category -> category.id }
            ) { category ->
                CategoryItem(category)
            }
        }
    }
}

@Composable
fun CategoryItem(category: CategoryEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconDisplay(iconKey = category.iconKey)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "ID: ${category.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryIconDisplay(iconKey: String?) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (iconKey == null) {
            Text(text = "📁", style = MaterialTheme.typography.headlineSmall)
        } else {
            when {
                iconKey.startsWith("emoji:") -> {
                    val emoji = iconKey.substringAfter("emoji:")
                    Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
                }
                iconKey.startsWith("uri:") -> {
                    val uri = iconKey.substringAfter("uri:")
                    AsyncImage(
                        model = uri,
                        contentDescription = "Category Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                iconKey.startsWith("res:") -> {
                    val resName = iconKey.substringAfter("res:")
                    // Temporary placeholder for resource icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resName.substringAfter("ic_").take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                else -> {
                    Text(text = "📁", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
