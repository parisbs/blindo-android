package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.device.DeviceGateway
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationCreateDevice(
    private val deviceGateway: DeviceGateway
) {
    suspend operator fun invoke(
        device: Device,
        idToken: String
    ): ApiResponse<Device> =
        deviceGateway.createDevice(device, idToken)
}
