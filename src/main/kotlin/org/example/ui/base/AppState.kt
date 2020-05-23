package org.example.ui.base

data class AppState(
    var currentConfigurationName: String = "",
    var configurationInChangeProcess: Boolean = false
)