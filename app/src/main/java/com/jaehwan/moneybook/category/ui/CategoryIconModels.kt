package com.jaehwan.moneybook.category.ui

import android.net.Uri

internal enum class IconSelectionType(val title: String) {
    EMOJI("이모지"),
    GALLERY("갤러리"),
    RESOURCE("기본"),
    NONE("없음"),
}

internal data class IconFormInitial(
    val type: IconSelectionType,
    val emoji: String,
    val galleryUri: Uri?,
    val resourceKey: String,
)

internal fun iconKeyToFormInitial(iconKey: String?): IconFormInitial {
    if (iconKey.isNullOrBlank()) {
        return IconFormInitial(IconSelectionType.NONE, "", null, "ic_food")
    }
    return when {
        iconKey.startsWith("emoji:") -> IconFormInitial(
            IconSelectionType.EMOJI,
            emoji = iconKey.removePrefix("emoji:"),
            galleryUri = null,
            resourceKey = "ic_food"
        )
        iconKey.startsWith("uri:") -> {
            val raw = iconKey.removePrefix("uri:")
            val uri = runCatching { Uri.parse(raw) }.getOrNull()
            IconFormInitial(IconSelectionType.GALLERY, "", galleryUri = uri, resourceKey = "ic_food")
        }
        iconKey.startsWith("res:") -> IconFormInitial(
            IconSelectionType.RESOURCE,
            emoji = "",
            galleryUri = null,
            resourceKey = iconKey.removePrefix("res:")
        )
        else -> IconFormInitial(IconSelectionType.NONE, "", null, "ic_food")
    }
}
