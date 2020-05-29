package org.example.ui.main_screen

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Observable.just
import org.example.App
import org.example.life.Configuration
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
        saveConfigBlocking(configuration)
        return just(getConfigsListBlocking())
    }

    fun deleteConfig(configuration: Configuration?): Observable<List<Configuration>> {
        if (configuration != null) {
            File("$SAVED_CONFIGS_FOLDER${File.separatorChar}${configuration.fileName}.$APP_EXT").delete()
        }
        return just(getConfigsListBlocking())
    }

    fun getConfigsListBlocking(): List<Configuration> {
        val dir = File(SAVED_CONFIGS_FOLDER)
        if (!dir.isDirectory) {
            dir.delete()
            dir.mkdir()
        }
        val savedList = dir.listFiles().orEmpty()
            .filter { it.isFile && it.canRead() && it.canWrite() && it.extension == APP_EXT }
            .map { it.readText() }
            .map { gson.fromJson(it, Configuration::class.java) }

        return if (savedList.isEmpty()) listOf(defaultConfig().also { saveConfigBlocking(it) })
        else savedList
    }

    fun saveConfigBlocking(configuration: Configuration) {
        val json = Gson().toJson(configuration)
        val file = File("$SAVED_CONFIGS_FOLDER${File.separatorChar}${configuration.fileName}.$APP_EXT")
        file.delete()
        file.createNewFile()
        file.writeText(json)
    }

    fun createNewConfig(name: String): Observable<List<Configuration>> {
        defaultConfig(name).also { saveConfigBlocking(it) }
        return just(getConfigsListBlocking())
    }

    fun changeAttr(change: (Configuration) -> Configuration): Observable<List<Configuration>> {
        val config = getConfigByName(App.state.currentConfigurationName)
        val newConf = change(config)
        return saveConfig(config.fileName, newConf)
    }
}