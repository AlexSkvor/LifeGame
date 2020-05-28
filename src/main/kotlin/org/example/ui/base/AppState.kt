package org.example.ui.base

data class AppState(
    var currentConfigurationName: String = "",
    var currentSpeciesId: Int = 0,
    var gameStyle: Style = Style.WATCH,
    var iterations: Int = 0
) {
    enum class Style { WATCH, TIME }
}