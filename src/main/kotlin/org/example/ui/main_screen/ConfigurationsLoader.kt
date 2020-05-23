package org.example.ui.main_screen

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Observable.just
import javafx.scene.paint.Color
import org.example.alsoPrintDebug
import org.example.life.Configuration
import org.example.life.Level
import org.example.life.Mineral
import org.example.life.Species
import java.io.File

class ConfigurationsLoader {

    companion object {
        const val SAVED_CONFIGS_FOLDER = "SavedConfigurations"
        const val APP_EXT = "life_game_temp_file"
    }

    private val gson = Gson()

    fun getConfigByName(name: String): Configuration {
        val file = File(SAVED_CONFIGS_FOLDER)
            .listFiles()
            .orEmpty()
            .filter { it.isFile && it.canRead() && it.canWrite() && it.extension == APP_EXT }
            .first { it.name == "$name.$APP_EXT" }

        val text = file.readText()
        return gson.fromJson(text, Configuration::class.java)
    }

    fun saveConfig(oldName: String?, configuration: Configuration?): Observable<List<Configuration>> {
        requireNotNull(configuration)
        if (oldName != null && oldName != configuration.fileName)
            File("$SAVED_CONFIGS_FOLDER${File.separatorChar}$oldName.$APP_EXT").delete()
        saveConfigInner(configuration)
        return getConfigsList()
    }

    fun deleteConfig(configuration: Configuration?): Observable<List<Configuration>> {
        if (configuration != null) {
            File("$SAVED_CONFIGS_FOLDER${File.separatorChar}${configuration.fileName}.$APP_EXT").delete()
        }
        return getConfigsList()
    }

    fun getConfigsList(): Observable<List<Configuration>> {
        val dir = File(SAVED_CONFIGS_FOLDER)
        if (!dir.isDirectory) {
            dir.delete()
            dir.mkdir()
        }
        val savedList = dir.listFiles().orEmpty()
            .filter { it.isFile && it.canRead() && it.canWrite() && it.extension == APP_EXT }
            .map { it.readText() }
            .map { gson.fromJson(it, Configuration::class.java) }

        return if (savedList.isEmpty()) just(listOf(defaultConfig()))
        else just(savedList)
    }

    private fun saveConfigInner(configuration: Configuration) {
        val json = Gson().toJson(configuration)
        val file = File("$SAVED_CONFIGS_FOLDER${File.separatorChar}${configuration.fileName}.$APP_EXT")
        file.delete()
        file.createNewFile()
        file.writeText(json)
    }

    fun createNewConfig(name: String): Observable<List<Configuration>> {
        defaultConfig(name)
        return getConfigsList()
    }

    private fun defaultConfig(name: String = "defaultConfig"): Configuration {
        val mineral0 = Mineral(0, "Ресурс 1", Color.SILVER)
        val mineral1 = Mineral(1, "Ресурс 2", Color.ORANGE)

        return Configuration(
            minerals = arrayListOf(mineral0, mineral1),
            species = arrayListOf(getTreeSpecies(), getGrassSpecies()),
            stonePercent = 20,
            speciesPercent = 1,
            neededMineralsMultiplier = 7,
            onDeathMultiplier = 5,
            startMinerals = 4,
            seedsLifeTime = 50,
            width = 120,
            height = 80,
            fileName = name
        ).also { saveConfigInner(it) }
    }

    private fun getTreeSpecies(): Species {
        val level0 = Level(
            neededMineralsForUpgradeToNext = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsForStayAlive = mapOf(Pair(0, 1), Pair(1, 0)),
            additionalMineralsDropped = mapOf(Pair(0, 0), Pair(1, 1)),
            additionalSeedsDroppedChance = 10
        )

        val level1 = Level(
            neededMineralsForUpgradeToNext = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsForStayAlive = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsDropped = mapOf(Pair(0, 0), Pair(1, 1)),
            additionalSeedsDroppedChance = 20
        )

        val level2 = Level(
            neededMineralsForUpgradeToNext = mapOf(Pair(0, 100), Pair(1, 0)),
            additionalMineralsForStayAlive = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsDropped = mapOf(Pair(0, 0), Pair(1, 10)),
            additionalSeedsDroppedChance = 30
        )

        return Species(
            id = 0,
            name = "Вид 1",
            color = Color.BLUE,
            levels = arrayListOf(level0, level1, level2),
            needMineralsForStayAlive = mapOf(Pair(0, 1), Pair(1, 0)),
            alwaysMineralsDropped = mapOf(Pair(0, 0), Pair(1, 2)),
            alwaysDroppedSeedsChance = 10,
            mainFieldMultiplierDropAndWaste = 1
        )
    }

    private fun getGrassSpecies(): Species {
        val level0 = Level(
            neededMineralsForUpgradeToNext = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsForStayAlive = mapOf(Pair(0, 0), Pair(1, 1)),
            additionalMineralsDropped = mapOf(Pair(0, 1), Pair(1, 0)),
            additionalSeedsDroppedChance = 10
        )

        val level1 = Level(
            neededMineralsForUpgradeToNext = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsForStayAlive = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsDropped = mapOf(Pair(0, 1), Pair(1, 0)),
            additionalSeedsDroppedChance = 20
        )

        val level2 = Level(
            neededMineralsForUpgradeToNext = mapOf(Pair(0, 0), Pair(1, 1)),
            additionalMineralsForStayAlive = mapOf(Pair(0, 0), Pair(1, 0)),
            additionalMineralsDropped = mapOf(Pair(0, 10), Pair(1, 0)),
            additionalSeedsDroppedChance = 30
        )

        return Species(
            id = 1,
            name = "Вид 2",
            color = Color.GREEN,
            levels = arrayListOf(level0, level1, level2),
            needMineralsForStayAlive = mapOf(Pair(0, 0), Pair(1, 1)),
            alwaysMineralsDropped = mapOf(Pair(0, 2), Pair(1, 0)),
            alwaysDroppedSeedsChance = 10,
            mainFieldMultiplierDropAndWaste = 1
        )
    }

}