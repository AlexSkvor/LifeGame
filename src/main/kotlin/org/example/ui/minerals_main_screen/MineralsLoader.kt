package org.example.ui.minerals_main_screen

import io.reactivex.Observable
import javafx.scene.paint.Color
import org.example.App
import org.example.life.Configuration
import org.example.life.Level
import org.example.life.Mineral
import org.example.ui.main_screen.ConfigurationsLoader

class MineralsLoader(private val configLoader: ConfigurationsLoader) {

    fun renameMineral(oldName: String, newName: String): Observable<Configuration> {
        val config = configLoader.getConfigByName(App.state.currentConfigurationName)
        val newConfig = config.copy(minerals = ArrayList(config.minerals.map {
            if (it.name == oldName) it.copy(name = newName) else it
        }))

        return configLoader.saveConfig(null, newConfig)
            .map { it.first { conf -> conf.fileName == newConfig.fileName } }
    }

    fun deleteMineral(name: String): Observable<Configuration> {
        val oldConfig = configLoader.getConfigByName(App.state.currentConfigurationName)
        val deletingId = oldConfig.minerals.first { it.name == name }.id

        val newMinerals = oldConfig.minerals.filter { it.id != deletingId }.map {
            if (it.id < deletingId) it
            else it.copy(id = it.id - 1)
        }

        val newSpecies = oldConfig.species.map { oldSpecies ->
            oldSpecies.copy(
                needMineralsForStayAlive = oldSpecies.needMineralsForStayAlive.delete(deletingId),
                alwaysMineralsDropped = oldSpecies.alwaysMineralsDropped.delete(deletingId),
                levels = oldSpecies.levels.deleteMineral(deletingId)
            )
        }

        val newConfig = oldConfig.copy(
            minerals = ArrayList(newMinerals),
            species = ArrayList(newSpecies)
        )
        return configLoader.saveConfig(null, newConfig)
            .map { it.first { conf -> conf.fileName == newConfig.fileName } }
    }

    fun createMineral(name: String): Observable<Configuration> {
        val oldConfig = configLoader.getConfigByName(App.state.currentConfigurationName)
        val addingId = oldConfig.minerals.size

        val newMinerals = oldConfig.minerals + Mineral(addingId, name)

        val newSpecies = oldConfig.species.map { oldSpecies ->
            oldSpecies.copy(
                needMineralsForStayAlive = oldSpecies.needMineralsForStayAlive + Pair(addingId, 0),
                alwaysMineralsDropped = oldSpecies.alwaysMineralsDropped + Pair(addingId, 0),
                levels = oldSpecies.levels.addMineral(addingId)
            )
        }

        val newConfig = oldConfig.copy(
            minerals = ArrayList(newMinerals),
            species = ArrayList(newSpecies)
        )
        return configLoader.saveConfig(null, newConfig)
            .map { it.first { conf -> conf.fileName == newConfig.fileName } }
    }

    private fun List<Level>.deleteMineral(deletingId: Int): ArrayList<Level> = ArrayList(map { oldLevel ->
        oldLevel.copy(
            neededMineralsForUpgradeToNext = oldLevel.neededMineralsForUpgradeToNext.delete(deletingId),
            additionalMineralsForStayAlive = oldLevel.additionalMineralsForStayAlive.delete(deletingId),
            additionalMineralsDropped = oldLevel.additionalMineralsDropped.delete(deletingId)
        )
    })

    private fun List<Level>.addMineral(addingId: Int): ArrayList<Level> = ArrayList(map { oldLevel ->
        oldLevel.copy(
            neededMineralsForUpgradeToNext = oldLevel.neededMineralsForUpgradeToNext + Pair(addingId, 0),
            additionalMineralsForStayAlive = oldLevel.additionalMineralsForStayAlive + Pair(addingId, 0),
            additionalMineralsDropped = oldLevel.additionalMineralsDropped + Pair(addingId, 0)
        )
    })

    private fun Map<Int, Int>.delete(deletingId: Int): Map<Int, Int> = toList()
        .filter { it.first != deletingId }
        .map {
            if (it.first < deletingId) it
            else it.copy(first = it.first - 1)
        }.toMap()

}