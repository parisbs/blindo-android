package com.pbaltazar.blindo.data.device

import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface DeviceGateway {

    suspend fun getDevice(hardwareFingerprint: String, idToken: String): ApiResponse<Device>

    suspend fun createDevice(device: Device, idToken: String): ApiResponse<Device>

    suspend fun updateDevice(device: Device, idToken: String): ApiResponse<Device>
}
