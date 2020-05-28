package org.example.ui.base

data class AppState(
    var currentConfigurationName: String = "",
    var currentSpeciesId: Int = 0,
    var gameStyle: Style = Style.WATCH
) {
    enum class Style { WATCH, TIME }
}