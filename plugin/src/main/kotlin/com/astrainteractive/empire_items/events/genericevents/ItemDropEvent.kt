package com.astrainteractive.empire_items.events.genericevents

import com.astrainteractive.empire_items.di.empireItemsApiModule
import com.astrainteractive.empire_items.di.empireModelEngineApiModule
import com.astrainteractive.empire_items.api.items.BlockParser
import com.astrainteractive.empire_items.api.models_ext.generateItem
import com.astrainteractive.empire_items.api.models_ext.performDrop
import com.atrainteractive.empire_items.models.Loot
import com.atrainteractive.empire_items.models.yml_item.YmlItem
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.loot.Lootable
import ru.astrainteractive.astralibs.di.getValue
import ru.astrainteractive.astralibs.events.DSLEvent

class ItemDropEvent() {
    private val empireItemsAPI by empireItemsApiModule
    private val empireModelEngineAPI by empireModelEngineApiModule

    private val blockLocations: MutableList<Location> = mutableListOf()
    fun isDropHereAbused(customBlock: YmlItem?, block: Block): Boolean {
        if (customBlock?.block?.ignoreCheck != true) {
            if (blockLocations.contains(block.location))
                return true
            else
                blockLocations.add(block.location)
            if (blockLocations.size > 60)
                blockLocations.removeAt(0)
        }
        return false
    }


    val onFishingEvent = DSLEvent.event<PlayerFishEvent> { e ->
        val caught = e.caught ?: return@event
        this.empireItemsAPI.dropByDropFrom["PlayerFishEvent"]?.forEach {
            it.performDrop(caught.location)
        }
    }

    val onBlockBreak = DSLEvent.event<BlockBreakEvent>(eventPriority = EventPriority.HIGHEST) { e ->

//        if (!KProtectionLib.canBuild(e.player, e.block.location)) return@event

//        if (!KProtectionLib.canBreak(e.player, e.block.location)) return@event
        val block: Block = e.block
        val customBlockData = BlockParser.getBlockData(e.block)
        val customBlock = empireItemsAPI.itemYamlFilesByID.values.firstOrNull { it.block?.data!=null && it.block?.data == customBlockData }
        val customBlockId = customBlock?.id
        if (isDropHereAbused(customBlock, block)) return@event
        val dropFrom = customBlockId ?: block.blockData.material.name
        if (e.isCancelled) return@event
        empireItemsAPI.dropByDropFrom[dropFrom]?.forEach {
            it.performDrop(block.location)
        } ?: return@event
        e.isDropItems = false
    }


    val inventoryOpenEvent = DSLEvent.event<PlayerInteractEvent> { e ->
        if (e.action != Action.RIGHT_CLICK_BLOCK)
            return@event
        val block = e.clickedBlock ?: return@event
        val chest = block.state as? Chest ?: return@event
        val lootable = chest as? Lootable ?: return@event
        lootable.lootTable ?: return@event
        val items = empireItemsAPI.dropByDropFrom["PlayerInteractEvent"]?.mapNotNull(Loot::generateItem)?:return@event

        chest.blockInventory.addItem(*items.toTypedArray())
    }

    val onMobDeath = DSLEvent.event<EntityDeathEvent> { e ->
        val entityInfo = empireModelEngineAPI.entityTagHolder.get(e.entity)

        val dropFrom = entityInfo?.empireID ?: e.entity.type.name
        empireItemsAPI.dropByDropFrom[dropFrom]?.forEach {
            it.performDrop(e.entity.location)
        }
    }
}