package com.mobile360.banking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// CR-004: label for the connectivity state
fun networkLabel(online: Boolean): String = if (online) "Online" else "Offline"

// CR-004: network indicator chip
@Composable
fun NetworkIndicator(online: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xCC1B1B1B), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(if (online) Color(0xFF2E7D32) else Color(0xFFC62828), CircleShape)
        )
        Spacer(Modifier.width(5.dp))
        BasicText(
            networkLabel(online),
            style = TextStyle(color = Color.White, fontSize = 11.sp)
        )
    }
}
