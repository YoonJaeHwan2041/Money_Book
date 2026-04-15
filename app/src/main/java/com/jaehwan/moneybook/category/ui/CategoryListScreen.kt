package com.jaehwan.moneybook.category.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.category.data.local.CategoryEntity

@Composable
fun CategoryList(
    categories: List<CategoryEntity>,
    onEdit: (CategoryEntity) -> Unit,
    onDeleteRequest: (CategoryEntity) -> Unit,
) {
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
                CategoryItem(
                    category = category,
                    onEdit = { onEdit(category) },
                    onDeleteRequest = { onDeleteRequest(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "ID: ${category.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onEdit) {
                    Text("수정")
                }
                TextButton(onClick = onDeleteRequest) {
                    Text("삭제")
                }
            }
        }
    }
}
