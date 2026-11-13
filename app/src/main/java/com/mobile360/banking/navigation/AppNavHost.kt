package com.mobile360.banking.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobile360.banking.AppGraph
import com.mobile360.banking.ui.components.BankBottomBar
import com.mobile360.banking.ui.screens.AccountsAndCardsScreen
import com.mobile360.banking.ui.screens.ClientDetailScreen
import com.mobile360.banking.ui.screens.ClientListScreen
import com.mobile360.banking.ui.screens.CoachScreen
import com.mobile360.banking.ui.screens.ConfirmAndReceiptScreen
import com.mobile360.banking.ui.screens.HelpAndSupportScreen
import com.mobile360.banking.ui.screens.HomeScreen
import com.mobile360.banking.ui.screens.MoreAndSettingsScreen
import com.mobile360.banking.ui.screens.PayScreen
import com.mobile360.banking.ui.screens.PrivacyAndDevicesScreen
import com.mobile360.banking.ui.screens.SignInScreen
import com.mobile360.banking.ui.screens.TradeScreen
import com.mobile360.banking.ui.screens.VaultScreen
import com.mobile360.banking.ui.screens.VerifyMFAScreen
import com.mobile360.banking.ui.screens.WealthOverviewScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.mobile360.banking.data.ConnectivityObserver
import com.mobile360.banking.ui.components.NetworkIndicator

private fun NavHostController.goTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost() {
    var start by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { start = AppGraph.session.startRoute() }
    val startRoute = start ?: return

    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val current by nav.currentBackStackEntryAsState()
    val route = current?.destination?.route

    val signOut: () -> Unit = {
        scope.launch {
            AppGraph.session.signOut()
            nav.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
        }
    }

    val context = LocalContext.current
val observer = remember { ConnectivityObserver(context) }
val online by observer.online.collectAsState(initial = false)
Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.fillMaxSize()) {
            NavHost(nav, startDestination = startRoute, modifier = Modifier.weight(1f)) {
                composable(Routes.SIGN_IN) {
                    SignInScreen(onOpenVerifyMFA = { nav.navigate(Routes.VERIFY_M_F_A) })
                }
                composable(Routes.VERIFY_M_F_A) {
                    VerifyMFAScreen(
                        onBack = { nav.popBackStack() },
                        onOpenHome = { nav.navigate(Routes.HOME) { popUpTo(Routes.SIGN_IN) { inclusive = true } } },
                        onOpenClientList = { nav.navigate(Routes.CLIENT_LIST) { popUpTo(Routes.SIGN_IN) { inclusive = true } } }
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenWealthOverview = { nav.navigate(Routes.WEALTH_OVERVIEW) },
                        onOpenAccountsAndCards = { nav.goTab(Routes.ACCOUNTS_AND_CARDS) },
                        onOpenPay = { nav.goTab(Routes.PAY) },
                        onOpenCoach = { nav.goTab(Routes.COACH) },
                        onOpenVault = { nav.navigate(Routes.VAULT) },
                        onOpenMoreAndSettings = { nav.goTab(Routes.MORE_AND_SETTINGS) }
                    )
                }
                composable(Routes.ACCOUNTS_AND_CARDS) {
                    AccountsAndCardsScreen(
                        onOpenWealthOverview = { nav.navigate(Routes.WEALTH_OVERVIEW) },
                        onOpenPay = { nav.navigate(Routes.PAY) }
                    )
                }
                composable(Routes.WEALTH_OVERVIEW) {
                    WealthOverviewScreen(onBack = { nav.popBackStack() })
                }
                composable(Routes.COACH) { CoachScreen() }
                composable(Routes.PAY) {
                    PayScreen(
                        onBack = { nav.popBackStack() },
                        onOpenConfirmAndReceipt = { id -> nav.navigate(Routes.confirmAndReceipt(id)) }
                    )
                }
                composable(
                    Routes.CONFIRM_AND_RECEIPT_PATTERN,
                    arguments = listOf(navArgument(Routes.ARG_RECIPIENT_ID) { type = NavType.StringType })
                ) { entry ->
                    ConfirmAndReceiptScreen(
                        recipientId = entry.arguments?.getString(Routes.ARG_RECIPIENT_ID).orEmpty(),
                        onBack = { nav.popBackStack() }
                    )
                }
                composable(Routes.VAULT) { VaultScreen(onBack = { nav.popBackStack() }) }
                composable(Routes.MORE_AND_SETTINGS) {
                    MoreAndSettingsScreen(
                        onOpenPrivacyAndDevices = { nav.navigate(Routes.PRIVACY_AND_DEVICES) },
                        onOpenHelpAndSupport = { nav.navigate(Routes.HELP_AND_SUPPORT) },
                        onSignOut = signOut
                    )
                }
                composable(Routes.HELP_AND_SUPPORT) { HelpAndSupportScreen(onBack = { nav.popBackStack() }) }
                composable(Routes.PRIVACY_AND_DEVICES) { PrivacyAndDevicesScreen(onBack = { nav.popBackStack() }) }
                composable(Routes.CLIENT_LIST) {
                    ClientListScreen(
                        onOpenClientDetail = { id -> nav.navigate(Routes.clientDetail(id)) },
                        onSignOut = signOut
                    )
                }
                composable(
                    Routes.CLIENT_DETAIL_PATTERN,
                    arguments = listOf(navArgument(Routes.ARG_CLIENT_ID) { type = NavType.StringType })
                ) { entry ->
                    ClientDetailScreen(
                        clientId = entry.arguments?.getString(Routes.ARG_CLIENT_ID).orEmpty(),
                        onBack = { nav.popBackStack() },
                        onOpenTrade = { id -> nav.navigate(Routes.trade(id)) }
                    )
                }
                composable(
                    Routes.TRADE_PATTERN,
                    arguments = listOf(navArgument(Routes.ARG_CLIENT_ID) { type = NavType.StringType })
                ) { entry ->
                    TradeScreen(
                        clientId = entry.arguments?.getString(Routes.ARG_CLIENT_ID).orEmpty(),
                        onBack = { nav.popBackStack() }
                    )
                }
            }
            if (route in Routes.bottomBarRoutes) {
                BankBottomBar(currentRoute = route, onSelect = { nav.goTab(it) })
            }
        }
        // CR-004: network indicator, top right
        NetworkIndicator(
            online = online,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 8.dp)
        )
    }
}
