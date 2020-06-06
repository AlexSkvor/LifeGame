package org.example.life

import javafx.scene.paint.Color

data class Mineral(
    val id: Int,
    val name: String
)

fun Map<Int, Int>.updateValue(key: Int, newValue: Int): Map<Int, Int> =
    toList().map { if (it.first == key) it.copy(second = newValue) else it }.toMap()