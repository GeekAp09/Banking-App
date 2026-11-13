package com.mobile360.banking.navigation

object Routes {
    const val SIGN_IN = "sign_in"
    const val VERIFY_M_F_A = "verify_m_f_a"
    const val HOME = "home"
    const val ACCOUNTS_AND_CARDS = "accounts_and_cards"
    const val WEALTH_OVERVIEW = "wealth_overview"
    const val COACH = "coach"
    const val PAY = "pay"
    const val CONFIRM_AND_RECEIPT = "confirm_and_receipt"
    const val VAULT = "vault"
    const val MORE_AND_SETTINGS = "more_and_settings"
    const val HELP_AND_SUPPORT = "help_and_support"
    const val PRIVACY_AND_DEVICES = "privacy_and_devices"
    const val CLIENT_LIST = "client_list"
    const val CLIENT_DETAIL = "client_detail"
    const val TRADE = "trade"

    const val ARG_RECIPIENT_ID = "recipientId"
    const val ARG_CLIENT_ID = "clientId"

    const val CONFIRM_AND_RECEIPT_PATTERN = "$CONFIRM_AND_RECEIPT/{$ARG_RECIPIENT_ID}"
    const val CLIENT_DETAIL_PATTERN = "$CLIENT_DETAIL/{$ARG_CLIENT_ID}"
    const val TRADE_PATTERN = "$TRADE/{$ARG_CLIENT_ID}"

    fun confirmAndReceipt(recipientId: String) = "$CONFIRM_AND_RECEIPT/$recipientId"
    fun clientDetail(clientId: String) = "$CLIENT_DETAIL/$clientId"
    fun trade(clientId: String) = "$TRADE/$clientId"

    /** Routes that show the shared bottom navigation bar. */
    val bottomBarRoutes = setOf(HOME, ACCOUNTS_AND_CARDS, WEALTH_OVERVIEW, COACH, PAY, MORE_AND_SETTINGS)

    // CR-002
    const val FEEDBACK = "feedback"
}
