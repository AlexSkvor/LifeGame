package org.example.life

data class Configuration(
    val minerals: ArrayList<Mineral>,
    val species: ArrayList<Species>,
    val stonePercent: Int,
    val speciesPercent: Int,
    val neededMineralsMultiplier: Int,
    val onDeathMultiplier: Int,
    val startMinerals: Int,
    val seedsLifeTime: Int,
    val width: Int,
    val height: Int
) {
    fun getDefaultMineralsMapCopy(): MutableMap<Int, Int> =
        minerals.map { it.id to startMinerals }.toMap().toMutableMap()
}