package com.mobile360.banking.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mobile360.banking.AppGraph
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.unit.IntOffset

// CR-012: one coachmark per bottom-bar tab, in tab order.
data class TourStep(val tab: String, val text: String)

object TourSteps {
    val all = listOf(
        TourStep("Home", "Home shows your balance and recent activity."),
        TourStep("Money", "Money lists your accounts and cards."),
        TourStep("Pay", "Pay sends money to people and bills."),
        TourStep("Coach", "Coach gives you simple tips on your spending."),
        TourStep("More", "More has settings, help and privacy."),
    )

    val count get() = all.size

    fun isLast(index: Int) = index >= all.lastIndex

    /** Next step index, or null when the tour is finished. */
    fun next(index: Int): Int? = if (isLast(index)) null else index + 1
}

/** Bounds of the bottom bar, reported by [tourBarAnchor]; null while the bar is not shown. */
object TourAnchors {
    var bar by mutableStateOf<Rect?>(null)
}

fun Modifier.tourBarAnchor(): Modifier = composed {
    DisposableEffect(Unit) { onDispose { TourAnchors.bar = null } }
    onGloballyPositioned { TourAnchors.bar = it.boundsInRoot() }
}

/** Shows the tour for a signed-in customer on a bottom-bar route until it is seen. */
@Composable
fun CoachmarkTourHost() {
    val session = AppGraph.session
    val seen by session.tourSeen.collectAsState(initial = true)
    val state by session.session.collectAsState(initial = null)
    val bar = TourAnchors.bar
    val s = state
    if (seen || bar == null || s == null || !s.signedIn || s.isWealthManager) return
    val scope = rememberCoroutineScope()
    CoachmarkTour(bar = bar, onFinish = { scope.launch { session.markTourSeen() } })
}

@Composable
fun CoachmarkTour(bar: Rect, onFinish: () -> Unit) {
    var index by remember { mutableStateOf(0) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var height by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val step = TourSteps.all[index]
    val tabWidth = bar.width / TourSteps.count
    val tab = Rect(
        bar.left + tabWidth * index - origin.x, bar.top - origin.y,
        bar.left + tabWidth * (index + 1) - origin.x, bar.bottom - origin.y,
    )
    val scrim = Color.Black.copy(alpha = 0.65f)
    val arrowHalf = 12.dp
    val sidePad = 16.dp

    BackHandler(onBack = onFinish)
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                origin = it.boundsInRoot().topLeft
                height = it.size.height.toFloat()
            }
            .pointerInput(Unit) { detectTapGestures { } }
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawBehind {
                drawRect(scrim)
                drawRoundRect(
                    Color.Transparent, tab.topLeft, Size(tab.width, tab.height),
                    CornerRadius(16.dp.toPx()), blendMode = BlendMode.Clear,
                )
            },
    ) {
        val bottomPad = with(density) { (height - tab.top).toDp() } + 8.dp
        val arrowX = with(density) { (tab.center.x).toDp() } - arrowHalf - sidePad
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(start = sidePad, end = sidePad, bottom = bottomPad),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Welcome tour, step ${index + 1} of ${TourSteps.count}: ${step.text}" },
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Welcome tour", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                    Text("Step ${index + 1} of ${TourSteps.count}", style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
                    Text(step.text, style = MaterialTheme.typography.bodyMedium, color = Color.Black, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onFinish) { Text("Skip") }
                        Button(onClick = { TourSteps.next(index)?.let { index = it } ?: onFinish() }) {
                            Text(if (TourSteps.isLast(index)) "Done" else "Next")
                        }
                    }
                }
            }
            Canvas(
                Modifier
                    .offset { IntOffset(arrowX.roundToPx().coerceAtLeast(0), 0) }
                    .width(arrowHalf * 2)
                    .height(10.dp),
            ) {
                val p = Path().apply {
                    moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2, size.height); close()
                }
                drawPath(p, Color.White)
            }
        }
    }
}
