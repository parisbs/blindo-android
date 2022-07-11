package com.blindo.screenshotwatcher.exceptions

class PermissionException(
    message: String = "Required permission is not granted."
) : Exception(message)
