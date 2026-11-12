package com.mobile360.banking.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
fun SignInScreen(
    onOpenVerifyMFA: () -> Unit,
    viewModel: SignInViewModel = viewModel(factory = vmFactory { SignInViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pin by rememberSaveable { mutableStateOf("") }
    val onSurface = Color(0xFF171C20)
    val variant = Color(0xFF43474D)
    val outline = Color(0xFF73777E)

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF6FAFF)).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).background(AppColors.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.AccountBalance, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(8.dp))
            Text("Banking", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.Notifications, "Notifications", tint = AppColors.secondary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp), color = Color(0xFFF0F4F9),
            border = BorderStroke(1.dp, Color(0xFFC3C6CE).copy(alpha = 0.4f)), modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(AppColors.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.SystemUpdate, null, tint = AppColors.onSecondaryContainer, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Update available: version 2.4", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = onSurface)
                    Text("Institutional security update recommended", fontSize = 12.sp, color = variant)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = outline)
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Box(
                    Modifier.size(80.dp).border(2.dp, AppColors.primaryContainer.copy(alpha = 0.2f), CircleShape).padding(4.dp)
                        .background(AppColors.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("A", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold) }
                Box(
                    Modifier.align(Alignment.BottomEnd).size(24.dp).background(AppColors.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.VerifiedUser, null, tint = AppColors.onSecondaryContainer, modifier = Modifier.size(14.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Text("Welcome back, Amara", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = onSurface)
            Text(
                "Authenticate to access your portfolio and private accounts",
                fontSize = 16.sp, color = variant, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 280.dp).padding(top = 4.dp)
            )
            Text(
                "Local demo sign-in: nothing is sent to your phone. The demo code is 123456.",
                fontSize = 12.sp, color = AppColors.secondary, textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp).padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            "Sign in with fingerprint or face", { viewModel.proceed(onOpenVerifyMFA) },
            modifier = Modifier.fillMaxWidth().height(52.dp), icon = Icons.Filled.Fingerprint, enabled = !state.loading
        )
        state.error?.let {
            Text(it, color = AppColors.error, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFC3C6CE).copy(alpha = 0.4f))
            Text("OR ENTER PIN", fontSize = 12.sp, color = outline, modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFC3C6CE).copy(alpha = 0.4f))
        }
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = pin,
            onValueChange = { v ->
                val d = v.filter { it.isDigit() }.take(6)
                pin = d
                viewModel.pinChanged(d, onOpenVerifyMFA)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "PIN entry, 6 digits" },
            decorationBox = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    repeat(6) { i ->
                        val filled = i < pin.length
                        Box(
                            Modifier.size(48.dp).background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, if (filled) outline else Color(0xFFC3C6CE).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(12.dp).background(if (filled) AppColors.primaryContainer else Color(0xFFC3C6CE), CircleShape))
                        }
                    }
                }
            }
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { viewModel.proceed(onOpenVerifyMFA) }, modifier = Modifier.heightIn(min = 48.dp), enabled = !state.loading) {
                Text("Use PIN or code instead", color = AppColors.secondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (state.loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Verified, null, tint = AppColors.secondary, modifier = Modifier.size(15.dp))
            Text(" FDIC Insured", fontSize = 12.sp, color = variant)
            Text(" • ", fontSize = 12.sp, color = Color(0xFFC3C6CE))
            Icon(Icons.Filled.Lock, null, tint = AppColors.secondary, modifier = Modifier.size(15.dp))
            Text(" 256-Bit Hardware Security", fontSize = 12.sp, color = variant)
        }
    }
}
