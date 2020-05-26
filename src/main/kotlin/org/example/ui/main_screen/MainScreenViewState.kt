package org.example.ui.main_screen

import org.example.life.Configuration

data class MainScreenViewState(
    val configsList: List<Configuration>,
    val chosenConfiguration: Configuration = configsList.first()
)