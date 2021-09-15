package com.pbaltazar.blindo.entities.errors

sealed class BillingException {

    object Disconnected : BillingException()
    object EmptyResponse : BillingException()
    object CanceledByUser : BillingException()
    object ServiceUnavailable : BillingException()
    object FeatureNotSupported : BillingException()
    class InstanceError(val error: String) : BillingException()
    class UnknownError(val error: String) : BillingException()
}
