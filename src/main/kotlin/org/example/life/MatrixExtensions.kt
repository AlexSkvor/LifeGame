package org.example.life

import org.example.randomOrder
import kotlin.random.Random

private val rand = Random(System.currentTimeMillis())

fun create(config: Configuration): CellCommon {
    var current = 0
    val random = rand.nextInt(0, 100)
    if (random in current until config.stonePercent)
        return CellCommon.Stone
    current += config.stonePercent

    config.species.randomOrder().forEach{ species ->
        if (random in current..(current + config.speciesPercent))
            return CellCommon.Cell(
                seeds = config.species.map { it.id to 0 }.toMap().toMutableMap(),
                currentInstance = null,
                minerals = config.getDefaultMineralsMapCopy()
            ).also { it.seeds[species.id] = 1 }
        else current += config.speciesPercent
    }

    return CellCommon.Cell(
        seeds = config.species.map { it.id to 0 }.toMap().toMutableMap(),
        currentInstance = null,
        minerals = config.getDefaultMineralsMapCopy()
    )
}

fun List<List<CellCommon>>.getNeighbours(height: Int, width: Int, i: Int, j: Int): List<CellCommon.Cell> =
    listOfNotNull(
        getTopLeftNeighbour(i, j),
        getTopCenterNeighbour(i, j),
        getTopRightNeighbour(i, j, width),
        getRightNeighbour(i, j, width),
        getBottomRightNeighbour(i, j, height, width),
        getBottomCenterNeighbour(i, j, height),
        getBottomLeftNeighbour(i, j, height),
        getLeftNeighbour(i, j)
    ).filterIsInstance<CellCommon.Cell>()

private fun List<List<CellCommon>>.getTopLeftNeighbour(i: Int, j: Int): CellCommon? =
    if (i == 0 || j == 0) null
    else this[i - 1][j - 1]

private fun List<List<CellCommon>>.getTopCenterNeighbour(i: Int, j: Int): CellCommon? =
    if (i == 0) null
    else this[i - 1][j]

private fun List<List<CellCommon>>.getTopRightNeighbour(i: Int, j: Int, width: Int): CellCommon? =
    if (i == 0 || j == width - 1) null
    else this[i - 1][j + 1]


private fun List<List<CellCommon>>.getBottomLeftNeighbour(i: Int, j: Int, height: Int): CellCommon? =
    if (i == height - 1 || j == 0) null
    else this[i + 1][j - 1]

private fun List<List<CellCommon>>.getBottomCenterNeighbour(i: Int, j: Int, height: Int): CellCommon? =
    if (i == height - 1) null
    else this[i + 1][j]

private fun List<List<CellCommon>>.getBottomRightNeighbour(i: Int, j: Int, height: Int, width: Int): CellCommon? =
    if (i == height - 1 || j == width - 1) null
    else this[i + 1][j + 1]


private fun List<List<CellCommon>>.getLeftNeighbour(i: Int, j: Int): CellCommon? =
    if (j == 0) null
    else this[i][j - 1]

private fun List<List<CellCommon>>.getRightNeighbour(i: Int, j: Int, width: Int): CellCommon? =
    if (j == width - 1) null
    else this[i][j + 1]