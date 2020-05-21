package org.example.life

import javafx.scene.paint.Color

data class Species(
    val id: Int,
    val name: String,
    val color: Color,
    val levels: ArrayList<Level>,
    val needMineralsForStayAlive: Map<Int, Int>,
    val alwaysMineralsDropped: Map<Int, Int>,
    val alwaysDroppedSeedsChance: Int,
    val mainFieldMultiplierDropAndWaste: Int
) {
    fun getInstance() = SpeciesInstance(id, state = SpeciesInstance.State.DEAD)
}