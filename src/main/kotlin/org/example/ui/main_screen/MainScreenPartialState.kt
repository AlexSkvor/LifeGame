package org.example.ui.main_screen

import org.example.life.Configuration

sealed class MainScreenPartialState(private val log: String) {

    data class ConfigsList(val list: List<Configuration>): MainScreenPartialState("ConfigsList $list")
    data class ConfigChosen(val config: Configuration): MainScreenPartialState("ConfigChosen $config")
    data class ConfigsListAfterSaving(val list: List<Configuration>): MainScreenPartialState("ConfigsListAfterSaving $list")
    data class ConfigsListAfterDeleting(val list: List<Configuration>): MainScreenPartialState("ConfigsListAfterDeleting $list")

    data class NewConfigName(val name: String): MainScreenPartialState("NewConfigName $name")
    data class NewStonePercent(val percent: Int): MainScreenPartialState("NewStonePercent $percent")
    data class NewSpeciesPercent(val percent: Int): MainScreenPartialState("NewSpeciesPercent $percent")
    data class NewNeededMineralsMultiplier(val multiplier: Int): MainScreenPartialState("NewNeededMineralsMultiplier $multiplier")
    data class NewOnDeathMultiplier(val multiplier: Int): MainScreenPartialState("NewOnDeathMultiplier $multiplier")
    data class NewStartMinerals(val minerals: Int): MainScreenPartialState("NewStartMinerals $minerals")
    data class NewSeedsLifeTime(val time: Int): MainScreenPartialState("NewSeedsLifeTime $time")
    data class NewMapSize(val size: Int): MainScreenPartialState("NewMapSize $size")

    object StartEdit: MainScreenPartialState("StartEdit")
    object StopEdit: MainScreenPartialState("StopEdit")

    override fun toString(): String = log
    fun partial() = this
}