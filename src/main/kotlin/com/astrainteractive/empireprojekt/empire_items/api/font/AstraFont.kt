package com.astrainteractive.empireprojekt.empire_items.api.font

import com.astrainteractive.empireprojekt.empire_items.api.utils.getCustomItemsFiles
import org.bukkit.configuration.ConfigurationSection

data class AstraFont(
    val id:String,
    val path: String,
    val height: Int,
    val ascent: Int,
    val char:String,
    val blockSend:Boolean,
    val namespace: String
) {

    companion object {
        val startChar: String
            get() = "\u3400"
        fun getFonts() = getCustomItemsFiles()?.mapNotNull {
            val fileConfig = it.getConfig()
            val section = fileConfig.getConfigurationSection("fontImages")
            section?.getKeys(false)?.mapNotNull {
                getFont(section.getConfigurationSection(it),fileConfig.getString("namespace","minecraft")!!)
            }
        }?.flatten()?: listOf()

        private fun getFont(s:ConfigurationSection?, namespace:String = "minecraft"): AstraFont? {
            s?:return null
            val id = s.name
            val path = s.getString("path")?:return null
            val height = s.getInt("height",10)
            val ascent = s.getInt("ascent",13)
            val char = s.getString("char")?:return null
            val blockSend = s.getBoolean("blockSend",false)
            return AstraFont(
                id = id,
                path = path,
                height = height,
                ascent = ascent,
                char = char,
                blockSend = blockSend,
                namespace = namespace
            )

        }
    }
}