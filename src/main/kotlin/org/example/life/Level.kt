package org.example.life

data class Level(
    val neededMineralsForUpgradeToNext: Map<Int, Int>,
    val additionalMineralsForStayAlive: Map<Int, Int>,
    val additionalMineralsDropped: Map<Int, Int>,
    val additionalSeedsDroppedChance: Int
)