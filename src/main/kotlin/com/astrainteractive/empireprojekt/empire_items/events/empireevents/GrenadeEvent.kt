package com.astrainteractive.empireprojekt.empire_items.events.empireevents

import com.astrainteractive.astralibs.IAstraListener
import com.astrainteractive.empireprojekt.EmpirePlugin
import com.astrainteractive.empireprojekt.EmpirePlugin.Companion.instance
import com.astrainteractive.empireprojekt.empire_items.api.utils.BukkitConstants
import com.astrainteractive.empireprojekt.empire_items.api.utils.getPersistentData
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.RegionQuery
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType

class GrenadeEvent : IAstraListener {


    override fun onDisable() {
        ProjectileHitEvent.getHandlerList().unregister(this)
    }

    @EventHandler
    fun onProjectileHit(e: ProjectileHitEvent) {
        if (e.entity.shooter !is Player) return
        val player = e.entity.shooter as Player

        val itemStack = player.inventory.itemInMainHand
        val meta = itemStack.itemMeta ?: return

        val explosionPower = meta.getPersistentData(BukkitConstants.GRENADE_EXPLOSION_POWER) ?: return
        println("Player ${player.name} threw grenade at blockLocation=${e.hitBlock?.location} playerLocation=${player.location}")
        if (!allowExplosion(instance, e.entity.location))
            return
        generateExplosion(e.entity.location, explosionPower.toDouble())
        e.entity.world.spawnParticle(Particle.SMOKE_LARGE, e.entity.location, 300, 0.0, 0.0, 0.0, 0.2)
    }


    companion object {
        fun allowExplosion(plugin: EmpirePlugin, location: Location): Boolean {
            if (plugin.server.pluginManager.getPlugin("WorldGuard") != null) {
                val query: RegionQuery = WorldGuard.getInstance().platform.regionContainer.createQuery()
                val loc: com.sk89q.worldedit.util.Location = BukkitAdapter.adapt(location)
                return (query.testState(loc, null, Flags.CREEPER_EXPLOSION))
            }
            return true
        }

        fun generateExplosion(location: Location, power: Double) {
            location.world?.createExplosion(location, power.toFloat()) ?: return
        }
    }
}
