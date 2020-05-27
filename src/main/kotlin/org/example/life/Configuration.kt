package org.example.life

private const val WIDTH = 12
private const val HEIGHT = 8

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
    val height: Int,
    val fileName: String
) {
    fun getDefaultMineralsMapCopy(): MutableMap<Int, Int> =
        minerals.map { it.id to startMinerals }.toMap().toMutableMap()

    fun getDefaultMineralsMapCopyZeroValues(): MutableMap<Int, Int> =
        minerals.map { it.id to 0 }.toMap().toMutableMap()

    fun newWithSize(size: Int) = copy(
        width = WIDTH * size,
        height = HEIGHT * size
    )

    val mapSize: Int
        get() = width / WIDTH
}