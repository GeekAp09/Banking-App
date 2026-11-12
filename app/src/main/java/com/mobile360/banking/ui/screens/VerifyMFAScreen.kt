package com.mobile360.banking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.PrimaryButton
import com.mobile360.banking.ui.theme.AppColors

import androidx.compose.runtime.saveable.rememberSaveable
@Composable
fun VerifyMFAScreen(
    onBack: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenClientList: () -> Unit,
    viewModel: VerifyMFAViewModel = viewModel(factory = vmFactory { VerifyMFAViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var code by rememberSaveable { mutableStateOf("") }
    val onSurface = Color(0xFF171C20)
    val outline = Color(0xFF73777E)
    val lowSurface = Color(0xFFF0F4F9)
    val border = Color(0xFFC3C6CE)

    Column(Modifier.fillMaxSize().background(Color(0xFFF6FAFF))) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back", tint = AppColors.primary)
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.background(lowSurface, CircleShape).border(1.dp, border.copy(alpha = 0.3f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.VerifiedUser, null, tint = AppColors.secondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("DEMO 2FA", fontSize = 12.sp, color = AppColors.secondary, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).imePadding()) {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(64.dp).background(AppColors.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .border(1.dp, border.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.MarkEmailRead, null, tint = AppColors.primaryContainer, modifier = Modifier.size(30.dp)) }
                Spacer(Modifier.height(20.dp))
                Text("Enter your code", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.primary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Local demo sign-in: no code or SMS is sent.", fontSize = 16.sp, color = AppColors.secondary,
                    textAlign = TextAlign.Center
                )
                Text("Demo code: 123456", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = onSurface)
            }
            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                BasicTextField(
                    value = code,
                    onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    cursorBrush = SolidColor(Color.Transparent),
                    modifier = Modifier.widthIn(max = 344.dp).fillMaxWidth()
                        .semantics { contentDescription = "Enter the 6-digit demo code" },
                    decorationBox = {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(6) { i ->
                                val active = i == code.length
                                val filled = i < code.length
                                Box(
                                    Modifier.weight(1f).height(56.dp).background(Color.White, RoundedCornerShape(12.dp))
                                        .border(if (filled || active) 2.dp else 1.dp, if (filled || active) AppColors.primary else outline, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (filled) Text(code[i].toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = onSurface)
                                    else Box(Modifier.size(8.dp).background(border, CircleShape))
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.background(lowSurface, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, null, tint = AppColors.secondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    val m = state.secondsLeft / 60
                    val s = state.secondsLeft % 60
                    Text("Expires in ", fontSize = 12.sp, color = AppColors.secondary)
                    Text("$m:${s.toString().padStart(2, '0')}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = onSurface)
                }
                state.error?.let {
                    Surface(color = AppColors.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(it, color = AppColors.onErrorContainer, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                    }
                }
                state.info?.let {
                    Surface(color = AppColors.secondaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(it, color = AppColors.onSecondaryContainer, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                "Verify", { viewModel.verify(code, onOpenHome) },
                modifier = Modifier.fillMaxWidth().height(52.dp), icon = Icons.AutoMirrored.Filled.ArrowForward, enabled = !state.loading
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { code = ""; viewModel.resend() }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Reset demo code timer", color = AppColors.primaryContainer, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = border.copy(alpha = 0.3f))
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).background(lowSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, border.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { viewModel.continueAsWealthManager(onOpenClientList) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(32.dp).background(Color.White, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.SupervisedUserCircle, null, tint = AppColors.secondary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Continue as Wealth Manager", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                    Text("Switch to institutional portal", fontSize = 12.sp, color = AppColors.secondary)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = AppColors.secondary)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
