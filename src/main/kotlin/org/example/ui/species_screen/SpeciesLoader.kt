package org.example.ui.species_screen

import io.reactivex.Observable
import io.reactivex.Observable.just
import javafx.scene.paint.Color
import org.example.App
import org.example.life.Configuration
import org.example.life.Level
import org.example.life.Species
import org.example.life.updateValue
import org.example.ui.main_screen.ConfigurationsLoader

class SpeciesLoader {

    private val configLoader = ConfigurationsLoader()

    fun getConfig(): Configuration = configLoader.getConfigByName(App.state.currentConfigurationName)

    fun createSpecies(name: String): Observable<Configuration> {
        val config = getConfig()
        val species = Species(
            id = config.species.size,
            name = name,
            color = Color.RED,
            levels = arrayListOf(
                Level(
                    config.getDefaultMineralsMapCopyZeroValues(),
                    config.getDefaultMineralsMapCopyZeroValues(),
                    config.getDefaultMineralsMapCopyZeroValues(),
                    0
                )
            ),
            needMineralsForStayAlive = config.getDefaultMineralsMapCopyZeroValues(),
            alwaysMineralsDropped = config.getDefaultMineralsMapCopyZeroValues(),
            alwaysDroppedSeedsChance = 0,
            mainFieldMultiplierDropAndWaste = 0
        )
        configLoader.saveConfig(null, config.copy(species = ArrayList(config.species + species)))
        return just(getConfig())
    }

    fun deleteSpecies(name: String): Observable<Configuration> {
        val config = getConfig()
        val speciesId = config.species.firstOrNull { it.name == name }
        speciesId?.let {
            val newList = config.species.filter { s -> s.id != it.id }
                .map { s -> if (s.id < it.id) s else s.copy(id = s.id - 1) }
            configLoader.saveConfig(null, config.copy(species = ArrayList(newList)))
        }
        return just(getConfig())
    }

    fun renameSpecies(id: Int, newName: String): Observable<Configuration> = changeAttr(id) {
        it.copy(name = newName)
    }

    fun changeColor(id: Int, newColor: Color): Observable<Configuration> = changeAttr(id) {
        it.copy(color = newColor)
    }

    fun changeDroppedChance(id: Int, newDroppedChance: Int): Observable<Configuration> = changeAttr(id) {
        it.copy(alwaysDroppedSeedsChance = newDroppedChance)
    }

    fun changeMainFieldMultiplier(id: Int, newMainFieldMultiplier: Int): Observable<Configuration> = changeAttr(id) {
        it.copy(mainFieldMultiplierDropAndWaste = newMainFieldMultiplier)
    }

    fun changeMineralNeed(speciesId: Int, mineralId: Int, value: Int): Observable<Configuration> =
        changeAttr(speciesId) {
            it.copy(needMineralsForStayAlive = it.needMineralsForStayAlive.updateValue(mineralId, value))
        }

    fun changeMineralDrop(speciesId: Int, mineralId: Int, value: Int): Observable<Configuration> =
        changeAttr(speciesId) {
            it.copy(alwaysMineralsDropped = it.alwaysMineralsDropped.updateValue(mineralId, value))
        }

    private fun changeAttr(id: Int, change: (Species) -> Species): Observable<Configuration> {
        val config = getConfig()
        val newList = config.species.map {
            if (it.id == id) change(it) else it
        }
        configLoader.saveConfigBlocking(config.copy(species = ArrayList(newList)))
        return just(getConfig())
    }
}