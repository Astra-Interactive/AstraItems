package com.makeevrserg.empireprojekt.util.files

import com.makeevrserg.empireprojekt.EmpirePlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.IllegalArgumentException

public class FileManager(var plugin: EmpirePlugin, var configName:String){

    private var configFiles: File? = null
    private var dataConfig: FileConfiguration? = null

    init {
        saveDefaultConfig()
    }


    fun reloadConfig() {
        if (this.configFiles == null) this.configFiles = File(plugin.dataFolder, configName)
        dataConfig = YamlConfiguration.loadConfiguration(configFiles!!)
        val defaultStream = plugin.getResource(configName)
        if (defaultStream != null) {
            val defaultConfig = YamlConfiguration.loadConfiguration(InputStreamReader(defaultStream))
            this.dataConfig?.setDefaults(defaultConfig)
        }
    }

    fun getName(): String? {
        return configName
    }


    fun getConfig(): FileConfiguration? {
        if (this.dataConfig == null) reloadConfig()
        return this.dataConfig
    }

    fun LoadFiles() {
        configFiles = File(plugin.dataFolder, configName)
    }

    fun updateConfig(conf: FileConfiguration) {
        this.dataConfig = conf
    }

    fun saveConfig() {
        if (this.configFiles == null || this.dataConfig == null) return
        try {
            getConfig()?.save(this.configFiles!!)
        } catch (e: IOException) {
            println("${plugin.translations.SAVE_ERROR} $configName")
        }
    }

    fun saveDefaultConfig() {
        if (this.configFiles == null) this.configFiles = File(plugin.dataFolder, configName)
        try {
            if (!this.configFiles!!.exists()) plugin.saveResource(configName, false)
        }catch (e:IllegalArgumentException){
            println(plugin.translations.NONSTANDART_FILE)
        }
    }
}