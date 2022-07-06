package com.pbaltazar.blindo.entities.purchases

import com.android.billingclient.api.ProductDetails
import com.pbaltazar.blindo.entities.purchases.enums.ProductType

open class Product(
    val id: String,
    val type: ProductType,
    val name: String = "",
    val description: String = "",
    val offers: List<Offer>? = null,
    val originalProductDetailsObject: ProductDetails? = null
)
