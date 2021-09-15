package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.device.DeviceGateway
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetDevice(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke(
        hardwareFingerprint: String,
        idToken: String
    ): ApiResponse<Device> =
        deviceGateway.getDevice(hardwareFingerprint, idToken)
}
