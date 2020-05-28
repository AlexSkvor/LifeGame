package org.example.life

import javafx.scene.paint.Color
import org.example.doNothing
import org.example.randomOrder
import kotlin.random.Random

sealed class CellCommon {

    abstract fun color(): Color
    abstract fun countNextState(configuration: Configuration)
    abstract fun recalculateFields(configuration: Configuration)
    abstract fun updateToNextState()

    object Stone : CellCommon() {
        override fun color(): Color = Color.BLACK
        override fun countNextState(configuration: Configuration) = Unit
        override fun recalculateFields(configuration: Configuration) = Unit
        override fun updateToNextState() = Unit
    }

    data class Cell(
        val seeds: MutableMap<Int, Int>,
        var currentInstance: SpeciesInstance?,
        val minerals: MutableMap<Int, Int>
    ) : CellCommon() {

        private val wasteMinerals: MutableMap<Int, Int> = minerals.mapValues { 0 }.toMutableMap()
        private val dropMinerals: MutableMap<Int, Int> = minerals.mapValues { 0 }.toMutableMap()
        private var dropSeedsChance: Int = 0

        private lateinit var neighbours: List<Cell>
        fun setNeighbours(neighbours: List<Cell>) {
            this.neighbours = neighbours
        }

        private var color: Color = Color.WHITE

        override fun color(): Color = color

        private fun recalculateColor(configuration: Configuration) {
            val species = currentInstance
            if (species != null) {
                val color = configuration.species[species.speciesId].color
                val intensity =
                    (species.currentLevelNumber + 1).toDouble() / configuration.species[species.speciesId].levels.size
                this.color = Color.color(color.red, color.green, color.blue, intensity)
            } else {
                color = Color.WHITE
            }
        }

        override fun countNextState(configuration: Configuration) {
            dropSeedsChance = 0
            minerals.forEach { (key, _) ->
                wasteMinerals[key] = 0
                dropMinerals[key] = 0
            }
            currentInstance?.let { countNextStateFromInstance(it, configuration) }
                ?: countNextStateFromSeeds(configuration)
        }

        override fun recalculateFields(configuration: Configuration) {
            collectSeeds(configuration)
            collectMinerals(configuration)
            recalculateColor(configuration)
        }

        override fun updateToNextState() {
            when (currentInstance?.nextState) {
                SpeciesInstance.State.ALIVE -> {
                    currentInstance?.state = SpeciesInstance.State.ALIVE
                }
                SpeciesInstance.State.LEVEL_UP -> {
                    currentInstance?.state = SpeciesInstance.State.ALIVE
                    currentInstance?.currentLevelNumber = 1 + currentInstance?.currentLevelNumber!!
                }
                SpeciesInstance.State.DEAD -> {
                    currentInstance = null
                }
                null -> doNothing()
            }
        }

        private fun collectMinerals(configuration: Configuration) {
            minerals.forEach { (key, value) ->
                neighbours.forEach {
                    minerals[key] = value + it.dropMinerals[key]!! - it.wasteMinerals[key]!!
                }
            }

            val instance = currentInstance
            if (instance != null) {
                minerals.forEach { (key, value) ->
                    minerals[key] = value + (dropMinerals[key]!! - wasteMinerals[key]!!) * configuration
                        .species[instance.speciesId].mainFieldMultiplierDropAndWaste
                }
            }
        }

        private fun collectSeeds(configuration: Configuration) {
            neighbours.forEach {
                val instance = it.currentInstance
                val indicator = Random.nextInt(0, 100)
                if (indicator < it.dropSeedsChance && instance != null)
                    seeds[instance.speciesId] = configuration.seedsLifeTime
            }
            val instance = currentInstance
            val indicator = Random.nextInt(0, 100)
            if (instance != null && indicator < dropSeedsChance * configuration.species[instance.speciesId].mainFieldMultiplierDropAndWaste)
                seeds[instance.speciesId] = configuration.seedsLifeTime
            seeds.keys.forEach { seeds[it] = if (seeds[it] == 0) 0 else seeds[it]!! - 1 }
        }

        private fun countNextStateFromSeeds(configuration: Configuration) {
            val speciesId = seeds.filterValues { it > 0 }.keys.randomOrder()
                .firstOrNull {
                    val mainFieldMultiplier = configuration.species[it].mainFieldMultiplierDropAndWaste
                    val availableMinerals = getAvailableMinerals(mainFieldMultiplier)
                    val needsMineralsForBerth = configuration.species[it].needMineralsForStayAlive
                    enough(availableMinerals, needsMineralsForBerth, configuration)
                } ?: return

            wasteMinerals.forEach { (key, _) ->
                wasteMinerals[key] = configuration.species[speciesId].needMineralsForStayAlive[key]!!
            }
            currentInstance = configuration.species[speciesId].getInstance()
            currentInstance?.nextState = SpeciesInstance.State.ALIVE
            seeds[speciesId] = 0
        }

        private fun countNextStateFromInstance(instance: SpeciesInstance, configuration: Configuration) {
            val mainFieldMultiplier = configuration.species[instance.speciesId].mainFieldMultiplierDropAndWaste
            val availableMinerals = getAvailableMinerals(mainFieldMultiplier)
            val needsMineralsToStayAlive = needsMineralsToStayAlive(instance, configuration)

            currentInstance?.nextState =
                if (enough(availableMinerals, needsMineralsToStayAlive, configuration)) {
                    val needsMineralsToLevelUp =
                        needsMineralsToLevelUp(instance, configuration, needsMineralsToStayAlive)

                    if (!enough(availableMinerals, needsMineralsToLevelUp, configuration)) {
                        shouldWasteMinerals(needsMineralsToStayAlive)
                        SpeciesInstance.State.ALIVE
                    } else {
                        if (instance.currentLevelNumber != configuration.species[instance.speciesId].levels.size - 1) {
                            shouldWasteMinerals(needsMineralsToLevelUp)
                            SpeciesInstance.State.LEVEL_UP
                        } else SpeciesInstance.State.DEAD
                    }
                } else SpeciesInstance.State.DEAD

            countShouldDropMinerals(configuration)
            countDropSeeds(configuration)
        }

        private fun countDropSeeds(configuration: Configuration) {
            val instance = currentInstance ?: return
            if (instance.state == SpeciesInstance.State.DEAD) return

            val species = configuration.species[instance.speciesId]
            val level = species.levels[instance.currentLevelNumber]

            dropSeedsChance = species.alwaysDroppedSeedsChance
            dropSeedsChance += level.additionalSeedsDroppedChance

            if (instance.nextState == SpeciesInstance.State.DEAD)
                dropSeedsChance *= configuration.onDeathMultiplier
        }

        private fun shouldWasteMinerals(waste: Map<Int, Int>) {
            wasteMinerals.keys.forEach {
                wasteMinerals[it] = waste[it]!!
            }
        }

        private fun countShouldDropMinerals(configuration: Configuration) {
            val instance = currentInstance ?: return
            if (instance.state == SpeciesInstance.State.DEAD) return

            val species = configuration.species[instance.speciesId]
            val level = species.levels[instance.currentLevelNumber]

            species.alwaysMineralsDropped.forEach { (key, value) ->
                dropMinerals[key] = value + level.additionalMineralsDropped[key]!!
            }

            when (instance.nextState) {
                SpeciesInstance.State.ALIVE -> return
                SpeciesInstance.State.LEVEL_UP -> {
                    level.neededMineralsForUpgradeToNext.forEach { (key, value) ->
                        dropMinerals[key] = dropMinerals[key]!! + value
                    }
                }
                SpeciesInstance.State.DEAD -> {
                    dropMinerals.keys.forEach {
                        dropMinerals[it] = dropMinerals[it]!! * configuration.onDeathMultiplier
                    }
                }
            }
        }

        private val needsMineralsToStayAlive: MutableMap<Int, Int> = minerals.mapValues { 0 }.toMutableMap()
        private fun needsMineralsToStayAlive(instance: SpeciesInstance, configuration: Configuration): Map<Int, Int> {
            val species = configuration.species[instance.speciesId]
            val level = species.levels[instance.currentLevelNumber]

            species.needMineralsForStayAlive.forEach { (key, value) ->
                needsMineralsToStayAlive[key] = value + level.additionalMineralsForStayAlive[key]!!
            }

            return needsMineralsToStayAlive
        }

        private val needsMineralsToLevelUp: MutableMap<Int, Int> = minerals.mapValues { 0 }.toMutableMap()
        private fun needsMineralsToLevelUp(
            instance: SpeciesInstance,
            configuration: Configuration,
            forStayAlive: Map<Int, Int>
        ): Map<Int, Int> {
            configuration.species[instance.speciesId].levels[instance.currentLevelNumber]
                .neededMineralsForUpgradeToNext.forEach { (key, value) ->
                    needsMineralsToLevelUp[key] = (value + forStayAlive[key]!!) * configuration.neededMineralsMultiplier
                }
            return needsMineralsToLevelUp
        }

        private val availableMinerals: MutableMap<Int, Int> = minerals.mapValues { 0 }.toMutableMap()
        private fun getAvailableMinerals(mainFieldMultiplier: Int): Map<Int, Int> {
            availableMinerals.forEach { (key, _) ->
                availableMinerals[key] = mainFieldMultiplier * minerals[key]!!
            }
            availableMinerals.forEach { (key, _) ->
                neighbours.forEach {
                    availableMinerals[key] = availableMinerals[key]!! + it.minerals[key]!!
                }
            }

            return availableMinerals
        }

        private fun enough(available: Map<Int, Int>, needs: Map<Int, Int>, configuration: Configuration): Boolean {
            needs.forEach { (key, value) ->
                if (value * configuration.neededMineralsMultiplier > available[key]!!)
                    return false
            }
            return true
        }
    }
}