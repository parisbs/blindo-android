package com.pbaltazar.blindo.utils.accessibility

import com.pbaltazar.blindo.utils.vision.BlindoVisionBridge

object AccessibilityCapabilities {

    fun isBlindoVisionEnabled(): Boolean = BlindoVisionBridge.listener.isWatching()
}
