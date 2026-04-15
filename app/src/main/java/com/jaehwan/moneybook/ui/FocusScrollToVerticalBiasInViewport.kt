package com.jaehwan.moneybook.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 포커스된 입력의 세로 중심이, [viewportCoordinates]가 가리키는 스크롤 영역(보통 `verticalScroll` Column)의
 * **화면상 보이는 구간** 안에서 [verticalBias] 비율 위치에 오도록 [scrollState]를 스크롤합니다.
 *
 * - `verticalBias = 0.5f` → 뷰포트 세로 중앙에 입력 중심이 오도록 맞춤
 * - 키보드가 아래에 있으면 `0.35f`~`0.45f`처럼 조금 위로 두는 편이 잘 보일 때가 많음
 *
 * 조정 위치: 이 파일의 [DEFAULT_VERTICAL_BIAS], [SCROLL_RETRY_DELAYS_MS]
 */
fun Modifier.focusScrollToVerticalBiasInViewport(
    scrollState: ScrollState,
    viewportCoordinates: () -> LayoutCoordinates?,
    coroutineScope: CoroutineScope,
    verticalBias: Float = DEFAULT_VERTICAL_BIAS,
): Modifier = composed {
    var fieldCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    then(
        Modifier
            .onGloballyPositioned { fieldCoords = it }
            .onFocusChanged { state ->
                if (!state.isFocused) return@onFocusChanged
                coroutineScope.launch {
                    SCROLL_RETRY_DELAYS_MS.forEach { waitMs ->
                        if (waitMs > 0L) delay(waitMs)
                        scrollFieldCenterToBias(
                            scrollState = scrollState,
                            field = fieldCoords,
                            viewport = viewportCoordinates(),
                            bias = verticalBias,
                        )
                    }
                }
            }
    )
}

/** 세로 위치 비율 (0=위, 1=아래). 여기 숫자를 바꿔 “가운데보다 위/아래”를 조절하면 됩니다. */
const val DEFAULT_VERTICAL_BIAS = 0.40f

/**
 * 포커스 이후 스크롤 보정 재시도 간격(ms).
 * 누적이 아니라 각 단계에서 기다리는 시간입니다.
 * 맨 아래 입력이 가려지면 마지막 값을 240~320 정도로 더 키워 보세요.
 */
val SCROLL_RETRY_DELAYS_MS = listOf(0L, 90L, 150L, 220L)

private suspend fun scrollFieldCenterToBias(
    scrollState: ScrollState,
    field: LayoutCoordinates?,
    viewport: LayoutCoordinates?,
    bias: Float,
) {
    val f = field?.takeIf { it.isAttached } ?: return
    val v = viewport?.takeIf { it.isAttached } ?: return
    val fieldCenterY = f.boundsInRoot().center.y
    val vr = v.boundsInRoot()
    val targetY = vr.top + vr.height * bias
    val deltaPx = fieldCenterY - targetY
    val targetScroll = (scrollState.value + deltaPx.roundToInt()).coerceIn(0, scrollState.maxValue)
    scrollState.scrollTo(targetScroll)
}
