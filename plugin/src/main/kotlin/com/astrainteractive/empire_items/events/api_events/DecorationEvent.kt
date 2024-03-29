package com.astrainteractive.empire_items.events.api_events

import com.astrainteractive.empire_items.api.utils.empireID
import com.astrainteractive.empire_items.api.items.DecorationBlockAPI
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.event.block.Action
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import ru.astrainteractive.astralibs.events.DSLEvent

class DecorationEvent(
    private val decorationBlockAPI: DecorationBlockAPI
) {

    val playerInteractEntityEvent = DSLEvent.event<HangingPlaceEvent> { e ->
        if (e.entity !is ItemFrame)
            return@event
        val itemFrame: ItemFrame = e.entity as ItemFrame
        val id = e.itemStack?.clone()?.empireID ?: return@event
        decorationBlockAPI.placeBlock(id, itemFrame, e.player?.location ?: return@event, e.blockFace)
    }

    val decorationInteractEvent = DSLEvent.event<PlayerInteractEvent>{ e ->
        val block = e.clickedBlock ?: return@event
        if (block.type != Material.BARRIER)
            return@event
        if (e.action == Action.LEFT_CLICK_BLOCK && e.hand == EquipmentSlot.HAND)
            decorationBlockAPI.breakItem(block.location)
    }
}