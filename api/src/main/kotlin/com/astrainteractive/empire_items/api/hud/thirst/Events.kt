package com.astrainteractive.empire_items.api.hud.thirst

import ru.astrainteractive.astralibs.async.PluginScope
import ru.astrainteractive.astralibs.events.DSLEvent
import ru.astrainteractive.astralibs.events.EventListener
import com.astrainteractive.empire_items.api.EmpireItemsAPI
import com.astrainteractive.empire_items.api.hud.IHudValueProvider
import com.astrainteractive.empire_items.api.hud.PlayerHud
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import kotlin.math.abs

class ThirstEvent {
    private val amountMap = mutableMapOf<UUID, Float>()

    val provider = ThirstProvider {
        amountMap[it] ?: 1f
    }


    val onDrink = DSLEvent.event(PlayerItemConsumeEvent::class.java) { e ->
        when (e.item.type) {
            Material.POTION, Material.LINGERING_POTION, Material.SPLASH_POTION -> {
                amountMap[e.player.uniqueId] = 1f
            }

            else -> return@event
        }
    }
    val onMove = DSLEvent.event(PlayerMoveEvent::class.java) { e ->
        PluginScope.launch {
            var amount = amountMap[e.player.uniqueId] ?: 0f
            amount -= 0.000005f
            amount = amount.coerceIn(0f, 1f)
            amountMap[e.player.uniqueId] = amount
        }
    }

    val playerJoin = DSLEvent.event(PlayerJoinEvent::class.java) { e ->
        amountMap.remove(e.player.uniqueId)
    }
    val playerLeave = DSLEvent.event(PlayerQuitEvent::class.java) { e ->
        amountMap[e.player.uniqueId] = 0f
    }
}