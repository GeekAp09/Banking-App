package com.mobile360.banking.ui.screens

import androidx.compose.foundation.selection.selectable as foundationSelectable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

internal fun Modifier.selectable(selected: Boolean, role: Role, onClick: () -> Unit): Modifier =
    foundationSelectable(selected = selected, role = role, onClick = onClick)
