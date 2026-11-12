package com.mobile360.banking

import com.mobile360.banking.data.ClientEntity
import com.mobile360.banking.data.ConsentEntity
import com.mobile360.banking.data.DeviceEntity

/** Shared builders for the BOLT-03 story tests. */
object Bolt03Data {
    fun client(
        id: String = "cl-1",
        name: String = "Grace Mensah",
        aum: Long = 21_000_000L,
        risk: String = "Balanced",
        auth: String = "AUTHORISED",
    ) = ClientEntity(
        id = id, name = name, accountNo = "0001", strategy = "Growth", tag = "VIP",
        aumCents = aum, ytdPct = 1.5, mandate = "Discretionary", baseCurrency = "USD",
        riskProfile = risk, assessedAt = 1_000L, adviceNote = "", authStatus = auth,
        growthExposurePct = 40.0, riskThresholdPct = 50.0,
    )

    fun device(id: String, name: String, current: Boolean = false, blocked: Boolean = false) =
        DeviceEntity(id = id, name = name, isCurrent = current, lastActiveAt = 0L, appInfo = "1.0", blocked = blocked)

    fun consent(id: String, title: String, granted: Boolean) =
        ConsentEntity(id = id, title = title, description = "", granted = granted)
}
