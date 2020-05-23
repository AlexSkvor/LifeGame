package org.example.ui.base

sealed class GlobalAction(private val log: String) {

    object NewConfigurationChosen: GlobalAction("NewConfigurationChosen")
    object GameStarted: GlobalAction("GameStarted")

    override fun toString(): String = log
    fun partial() = this
}