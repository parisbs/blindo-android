package com.pbaltazar.blindo.utils.log

import timber.log.Timber

class BlindoDebugger : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String =
        "[Class: ${element.className} [Line: ${element.lineNumber} [Method: ${element.methodName}]] [Error: ${super.createStackElementTag(element)}]]"
}
