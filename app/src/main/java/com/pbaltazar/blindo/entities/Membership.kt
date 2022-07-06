package com.pbaltazar.blindo.entities

import com.pbaltazar.blindo.entities.enums.MembershipCancellationContext
import com.pbaltazar.blindo.entities.enums.MembershipState
import java.util.*

data class Membership(
    val id: String,
    val productId: String,
    val state: MembershipState,
    val isAutoRenew: Boolean,
    val cancellationContext: MembershipCancellationContext? = null,
    val autoResumeTime: Date? = null,
    val expireAt: Date,
    val purchases: List<Purchase>? = null
)
