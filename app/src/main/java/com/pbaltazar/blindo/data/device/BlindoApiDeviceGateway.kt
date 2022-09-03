package com.pbaltazar.blindo.data.device

import com.apollographql.apollo3.api.Optional
import com.blindo.apollito.api.ApollitoClient
import com.blindo.apollito.api.constants.FetchPolicy
import com.blindo.apollito.models.Response
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.CreateDeviceMutation
import com.pbaltazar.blindo.graphql.GetDeviceQuery
import com.pbaltazar.blindo.graphql.UpdateDeviceMutation
import com.pbaltazar.blindo.graphql.type.CreateDeviceInput
import com.pbaltazar.blindo.graphql.type.UpdateDeviceInput
import com.pbaltazar.blindo.utils.extensions.toApiModel

class BlindoApiDeviceGateway(
    private val blindoApiClient: ApollitoClient
) : ApiHelpers, DeviceGateway {

    override suspend fun getDevice(
        hardwareFingerprint: String,
        idToken: String
    ): ApiResponse<Device> =
        blindoApiClient.query(
            GetDeviceQuery(
                hardwareFingerprint = hardwareFingerprint
            ),
            idToken,
            FetchPolicy.NETWORK_ONLY
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getDevice?.let { device ->
                    ApiResponse.Success(device.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun createDevice(device: Device, idToken: String): ApiResponse<Device> =
        blindoApiClient.mutate(
            CreateDeviceMutation(
                input = CreateDeviceInput(
                    hardwareFingerprint = device.hardwareFingerprint,
                    gcmToken = Optional.presentIfNotNull(device.gcmToken),
                    name = device.name,
                    language = device.language,
                    country = device.country
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.createDevice?.device?.let { device ->
                    ApiResponse.Success(device.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun updateDevice(device: Device, idToken: String): ApiResponse<Device> =
        blindoApiClient.mutate(
            UpdateDeviceMutation(
                UpdateDeviceInput(
                    id = Optional.presentIfNotNull(device.id.takeUnless { it.isEmpty() }),
                    hardwareFingerprint = Optional.presentIfNotNull(device.hardwareFingerprint.takeUnless { it.isEmpty() }),
                    gcmToken = Optional.presentIfNotNull(device.gcmToken),
                    name = Optional.presentIfNotNull(device.name.takeUnless { it.isEmpty() }),
                    language = Optional.presentIfNotNull(device.language.takeUnless { it.isEmpty() }),
                    country = Optional.presentIfNotNull(device.country.takeUnless { it.isEmpty() })
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.updateDevice?.device?.let { updatedDevice ->
                    ApiResponse.Success(updatedDevice.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
