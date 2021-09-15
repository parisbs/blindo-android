package com.pbaltazar.blindo.entities.responses

import com.pbaltazar.blindo.entities.errors.BillingException

sealed class BillingResponse<out T> {

    class Success<T>(val data: T): BillingResponse<T>()
    class Error(val error: BillingException): BillingResponse<Nothing>()
}
