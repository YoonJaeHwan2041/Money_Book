package com.jaehwan.moneybook.category.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun CategoryIconDisplay(
    iconKey: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(modifier),
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
                    val context = LocalContext.current
                    val resId = context.resources.getIdentifier(
                        resName,
                        "drawable",
                        context.packageName,
                    )
                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Category Resource Icon",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
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
                }
                else -> {
                    Text(text = "📁", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
