package com.astrainteractive.empire_items.api.sounds

import com.astrainteractive.empire_items.api.utils.getCustomItemsFiles

data class AstraSounds(
    var id: String,
    var namespace: String,
    val sounds: List<String>
) {
    companion object {
        fun getSounds() = getCustomItemsFiles()?.mapNotNull files@{
            val fileConfig = it.getConfig()
            val section = fileConfig.getConfigurationSection("sounds")
            section?.getKeys(false)?.mapNotNull {
                val s = section.getConfigurationSection(it) ?: return@mapNotNull null
                AstraSounds(
                    id = s.name,
                    sounds = s.getStringList("sounds"),
                    namespace = fileConfig.getString("namespace", "empire_items")!!
                )
            }
        }?.flatten() ?: listOf()
    }
}