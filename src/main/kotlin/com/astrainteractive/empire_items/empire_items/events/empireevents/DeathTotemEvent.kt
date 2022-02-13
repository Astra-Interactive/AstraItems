package com.astrainteractive.empire_items.empire_items.events.empireevents

import com.astrainteractive.astralibs.AstraLibs
import com.astrainteractive.astralibs.HEX
import com.astrainteractive.astralibs.EventListener
import com.astrainteractive.empire_items.empire_items.api.utils.BukkitConstants
import com.astrainteractive.empire_items.empire_items.api.utils.hasPersistentData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class DeathTotemEvent : EventListener {

    private fun ItemStack?.isDeathTotem() =
        this?.itemMeta?.hasPersistentData(BukkitConstants.TOTEM_OF_DEATH) == true

    private fun isHoldTotem(inv:PlayerInventory): Boolean =
         inv.itemInMainHand.isDeathTotem() || inv.itemInOffHand.isDeathTotem()

    @EventHandler
    private fun playerInteractEvent(e:PlayerInteractEvent){
        if (isHoldTotem(e.player.inventory))
            killPlayer(e.player)
    }
    @EventHandler
    private fun playerItemHeldEvent(e: PlayerItemHeldEvent) {
        if (isHoldTotem(e.player.inventory))
            killPlayer(e.player)
    }

    private fun killPlayer(player:Player){
        player.damage(999999999999.0)
        Bukkit.getScheduler().runTaskLater(AstraLibs.instance, Runnable {
            player.damage(999999999999.0)

        },20L*2)
        player.sendMessage("#cf2d04${ChatColor.MAGIC}Голос ${ChatColor.AQUA}-> #cf2d04Ты недостоин".HEX())
    }
    override fun onDisable() {
        PlayerItemHeldEvent.getHandlerList().unregister(this)
        PlayerInteractEvent.getHandlerList().unregister(this)
    }
}