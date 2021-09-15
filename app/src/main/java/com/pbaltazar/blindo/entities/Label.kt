package com.pbaltazar.blindo.entities

data class Label(
    val packageName: String,
    val packageVersion: Int,
    val packageSignature: String,
    val viewName: String,
    val labelText: String,
    val language: String
)
