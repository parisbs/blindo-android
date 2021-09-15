package com.pbaltazar.blindo.data.device

import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
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
import com.wizeline.simpleapollo.api.SimpleApolloClient
import com.wizeline.simpleapollo.models.Response

class BlindoApiDeviceGateway(
    private val blindoApiClient: SimpleApolloClient
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
            HttpCachePolicy.NETWORK_ONLY
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
                    gcmToken = Input.optional(device.gcmToken),
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
                    id = device.id,
                    gcmToken = Input.optional(device.gcmToken),
                    name = Input.optional(device.name),
                    language = Input.optional(device.language),
                    country = Input.optional(device.country)
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
