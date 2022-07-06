package com.pbaltazar.blindo.entities.purchases.inapp

import com.android.billingclient.api.ProductDetails
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.enums.ProductType

class InApp(
    id: String,
    name: String = "",
    description: String = "",
    val offer: InAppOffer? = null,
    originalProductDetailsObject: ProductDetails? = null
) : Product(
    id,
    ProductType.INAPP,
    name,
    description,
    offer?.let { listOf(it) },
    originalProductDetailsObject
)
