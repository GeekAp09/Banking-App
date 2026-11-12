package com.mobile360.banking.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile360.banking.navigation.Routes
import com.mobile360.banking.ui.theme.AppColors

/** Top bar with optional back arrow and trailing actions (48dp touch targets). */
@Composable
fun BankTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(modifier = modifier, color = AppColors.surfaceContainerLowest) {
        Column {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                } else Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.inkDark,
                    modifier = Modifier.weight(1f)
                )
                actions()
            }
            HorizontalDivider(color = AppColors.brandBorder.copy(alpha = 0.15f))
        }
    }
}

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

val BottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home),
    BottomTab(Routes.ACCOUNTS_AND_CARDS, "Money", Icons.Filled.AccountBalanceWallet),
    BottomTab(Routes.PAY, "Pay", Icons.Filled.QrCodeScanner),
    BottomTab(Routes.COACH, "Coach", Icons.Filled.Psychology),
    BottomTab(Routes.MORE_AND_SETTINGS, "More", Icons.Filled.MoreHoriz)
)

@Composable
fun BankBottomBar(currentRoute: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        HorizontalDivider(color = AppColors.brandBorder.copy(alpha = 0.15f))
        NavigationBar(containerColor = AppColors.surfaceContainerLowest, windowInsets = WindowInsets(0, 0, 0, 0)) {
            BottomTabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = { onSelect(tab.route) },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = {
                        Text(
                            tab.label,
                            fontSize = if (tab.label.length > 10) 10.sp else 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.onTonalContainer,
                        selectedTextColor = AppColors.onTonalContainer,
                        indicatorColor = AppColors.tonalContainer,
                        unselectedIconColor = AppColors.labelMuted,
                        unselectedTextColor = AppColors.labelMuted
                    )
                )
            }
        }
    }
}

/** Level-1 card: white, 12dp radius, soft border. Clickable when onClick is given. */
@Composable
fun BankCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    container: Color = AppColors.surfaceContainerLowest,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val colors = CardDefaults.cardColors(containerColor = container)
    val border = BorderStroke(1.dp, AppColors.brandBorder.copy(alpha = 0.2f))
    val inner: @Composable ColumnScope.() -> Unit = { Column(Modifier.padding(16.dp), content = content) }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, border = border, content = inner)
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, border = border, content = inner)
    }
}

/** List row, min height 56dp, optional leading icon, subtitle, trailing slot and chevron. */
@Composable
fun BankListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    trailing: @Composable (() -> Unit)? = null
) {
    val click = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Row(
        modifier.fillMaxWidth().then(click).heightIn(min = 56.dp).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                Modifier.size(40.dp).background(AppColors.tonalContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = AppColors.onTonalContainer) }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = AppColors.inkDark)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppColors.labelMuted)
        }
        trailing?.invoke()
        if (showChevron) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.labelMuted)
    }
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier.padding(start = 16.dp), color = AppColors.brandBorder.copy(alpha = 0.12f))
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.inkDark, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.primaryContainer,
                modifier = Modifier.sizeIn(minHeight = 48.dp).clickable(role = Role.Button, onClick = onAction).padding(horizontal = 8.dp, vertical = 12.dp),
            )
        }
    }
}

/** Inline notice (update, offline, discrepancy). */
@Composable
fun Banner(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val bg = if (isError) AppColors.errorContainer else AppColors.secondaryContainer
    val fg = if (isError) AppColors.onErrorContainer else AppColors.onSecondaryFixed
    Row(
        modifier.fillMaxWidth().background(bg, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp).heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = fg)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = fg, modifier = Modifier.weight(1f))
        if (actionLabel != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                modifier = Modifier.sizeIn(minHeight = 48.dp).clickable(role = Role.Button, onClick = onAction).padding(horizontal = 8.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun BankChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(8.dp),
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = AppColors.surfaceContainerLowest,
            labelColor = AppColors.labelMuted,
            selectedContainerColor = AppColors.tonalContainer,
            selectedLabelColor = AppColors.onTonalContainer,
            selectedLeadingIconColor = AppColors.onTonalContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = AppColors.brandBorder,
            selectedBorderColor = AppColors.primaryContainer
        )
    )
}

@Composable
fun StatusBadge(text: String, modifier: Modifier = Modifier, container: Color = AppColors.tonalContainer, content: Color = AppColors.onTonalContainer) {
    Text(
        text,
        modifier = modifier.background(container, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = content
    )
}

@Composable
fun InitialsAvatar(initials: String, modifier: Modifier = Modifier, size: Int = 40) {
    Box(modifier.size(size.dp).background(AppColors.tonalContainer, CircleShape), contentAlignment = Alignment.Center) {
        Text(initials, style = MaterialTheme.typography.labelMedium, color = AppColors.onTonalContainer)
    }
}

private val ButtonPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(28.dp),
        contentPadding = ButtonPadding,
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryContainer, contentColor = AppColors.onPrimary)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
        if (icon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(icon, contentDescription = null)
        }
    }
}

@Composable
fun TonalButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(28.dp),
        contentPadding = ButtonPadding,
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.tonalContainer, contentColor = AppColors.onTonalContainer)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun OutlinedAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(28.dp),
        contentPadding = ButtonPadding,
        border = BorderStroke(1.dp, AppColors.brandBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.primaryContainer)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Outlined search field bound to a query held by the ViewModel. */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = trailing,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppColors.surfaceContainerLowest,
            unfocusedContainerColor = AppColors.surfaceContainerLowest,
            focusedBorderColor = AppColors.primaryContainer,
            unfocusedBorderColor = AppColors.brandBorder
        )
    )
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier, icon: ImageVector = Icons.Filled.Search) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.labelMuted, modifier = Modifier.size(40.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.inkDark, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodySmall, color = AppColors.labelMuted, textAlign = TextAlign.Center)
    }
}
