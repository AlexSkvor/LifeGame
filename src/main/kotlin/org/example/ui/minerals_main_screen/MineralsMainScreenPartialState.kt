package org.example.ui.minerals_main_screen

import org.example.life.Configuration

sealed class MineralsMainScreenPartialState {
    data class NewConfigurationState(val config: Configuration) : MineralsMainScreenPartialState()
}