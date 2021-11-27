package com.astrainteractive.empireprojekt.empire_items.api.items.data.block

import org.bukkit.configuration.ConfigurationSection

data class Block(
    val breakParticle:String?,
    val breakSound:String?,
    val placeSound:String?,
    val data:Int,
    val hardness:Int?,
    val generate: Generate?
){
    companion object{
        fun getBlock(s:ConfigurationSection?): Block? {
            s?:return null
            val breakParticle = s.getString("breakParticle")
            val breakSound = s.getString("breakSound")
            val placeSound = s.getString("placeSound")
            val data = s.getInt("data")
            val hardness = s.getInt("hardness")
            val generate = Generate.getGenerate(s.getConfigurationSection("generate"))
            return Block(
                breakParticle = breakParticle,
                breakSound = breakSound,
                placeSound = placeSound,
                data = data,
                hardness = hardness,
                generate = generate
            )
        }
    }
}