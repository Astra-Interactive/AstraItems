package com.makeevrserg.empireprojekt.essentials

import com.makeevrserg.empireprojekt.EmpirePlugin
import empirelibs.IEmpireListener
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent

class SpawnerEggBlockEvent:IEmpireListener {


    @EventHandler
    fun spawnerInteractEvent(e:PlayerInteractEvent){
        e.clickedBlock?:return
        if (e.clickedBlock!!.type!=Material.SPAWNER)
            return
        val itemInHand = e.player.inventory.itemInMainHand.type
        val itemInOffHand = e.player.inventory.itemInOffHand.type
        if (itemInHand.name.contains("egg",ignoreCase = true) || itemInOffHand.name.contains("egg",ignoreCase = true)){
            e.isCancelled = true
            return
        }
    }

    override fun onDisable() {
        PlayerInteractEvent.getHandlerList().unregister(this)

    }
}