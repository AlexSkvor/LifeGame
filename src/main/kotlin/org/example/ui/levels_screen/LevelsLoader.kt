package org.example.ui.levels_screen

import org.example.App
import org.example.life.Configuration
import org.example.life.Level
import org.example.life.Species
import org.example.life.updateValue
import org.example.ui.main_screen.ConfigurationsLoader

class LevelsLoader {

    private val configLoader = ConfigurationsLoader()

    fun getConfig(): Configuration = configLoader.getConfigByName(App.state.currentConfigurationName)

    fun addLevel(): Configuration = withCurrentSpecies { config, species ->
        val newLevel = Level(
            neededMineralsForUpgradeToNext = config.getDefaultMineralsMapCopyZeroValues(),
            additionalMineralsDropped = config.getDefaultMineralsMapCopyZeroValues(),
            additionalMineralsForStayAlive = config.getDefaultMineralsMapCopyZeroValues(),
            additionalSeedsDroppedChance = 0
        )
        val newLevels = species.levels + newLevel
        species.copy(levels = ArrayList(newLevels))
    }

    fun deleteLevel(num: Int): Configuration = withCurrentSpecies { _, species ->
        species.copy(
            levels = ArrayList(species.levels.filterIndexed { i, _ -> i != num })
        )
    }

    fun changeSeedsDropChance(levelNum: Int, newChance: Int) = withLevel(levelNum) { level ->
        level.copy(additionalSeedsDroppedChance = newChance)
    }

    fun changeMineralNeed(levelNum: Int, mineralId: Int, value: Int) = withLevel(levelNum) { level ->
        level.copy(additionalMineralsForStayAlive = level.additionalMineralsForStayAlive.updateValue(mineralId, value))
    }

    fun changeMineralDrop(levelNum: Int, mineralId: Int, value: Int) = withLevel(levelNum) { level ->
        level.copy(additionalMineralsDropped = level.additionalMineralsDropped.updateValue(mineralId, value))
    }

    fun changeMineralUpdate(levelNum: Int, mineralId: Int, value: Int) = withLevel(levelNum) { level ->
        level.copy(neededMineralsForUpgradeToNext = level.neededMineralsForUpgradeToNext.updateValue(mineralId, value))
    }

    private fun withLevel(levelNum: Int, action: (Level) -> Level) = withCurrentSpecies { _, species ->
        val newLevel = action(species.levels[levelNum])
        species.copy(levels = ArrayList(species.levels.mapIndexed { i, level -> if (i != levelNum) level else newLevel }))
    }

    private fun withCurrentSpecies(action: (Configuration, Species) -> Species): Configuration {
        val config = getConfig()
        val species = config.species[App.state.currentSpeciesId]
        val newSpecies = action(config, species)
        val newConfig = config.copy(
            species = ArrayList(
                config.species.map { if (it.id == newSpecies.id) newSpecies else it }
            )
        )
        return newConfig.also {
            configLoader.saveConfigBlocking(it)
        }
    }
}