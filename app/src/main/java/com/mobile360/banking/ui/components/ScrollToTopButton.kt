package com.mobile360.banking.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// CR-006: the button shows once the first item has scrolled out of view.
internal fun shouldShowScrollToTop(firstVisibleItemIndex: Int): Boolean = firstVisibleItemIndex > 0

// CR-006: floating "Back to top" button for a LazyColumn.
@Composable
fun ScrollToTopButton(state: LazyListState, modifier: Modifier = Modifier) {
    val show by remember(state) { derivedStateOf { shouldShowScrollToTop(state.firstVisibleItemIndex) } }
    val scope = rememberCoroutineScope()
    AnimatedVisibility(visible = show, modifier = modifier, enter = fadeIn(), exit = fadeOut()) {
        SmallFloatingActionButton(onClick = { scope.launch { state.animateScrollToItem(0) } }) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Back to top")
        }
    }
}

// CR-006: LazyColumn that hosts the floating "Back to top" button.
@Composable
fun ScrollToTopLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: LazyListScope.() -> Unit,
) {
    Box(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
        ScrollToTopButton(state, Modifier.align(Alignment.BottomEnd).padding(16.dp))
    }
}
