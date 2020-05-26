package org.example.ui.main_screen

import org.example.life.Configuration

sealed class MainScreenPartialState(private val log: String) {

    data class ConfigsList(val list: List<Configuration>): MainScreenPartialState("ConfigsList $list")
    data class ConfigChosen(val config: Configuration): MainScreenPartialState("ConfigChosen $config")

    override fun toString(): String = log
    fun partial() = this
}