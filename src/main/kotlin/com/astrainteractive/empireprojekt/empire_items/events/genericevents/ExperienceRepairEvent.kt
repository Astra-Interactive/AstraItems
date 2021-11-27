package com.astrainteractive.empireprojekt.empire_items.events.genericevents

import com.astrainteractive.astralibs.IAstraListener
import com.astrainteractive.empireprojekt.empire_items.api.utils.BukkitConstants

import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemMendEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class ExperienceRepairEvent : IAstraListener {



    @EventHandler
    fun repairEvent(e: PlayerItemMendEvent) {
        changeDurability(e.item, e.repairAmount)
    }

    @EventHandler
    fun durabilityEvent(e: PlayerItemDamageEvent) {
        changeDurability(e.item, -e.damage)
    }



    @EventHandler
    fun anvilEvent(e: PrepareAnvilEvent) {
        val itemStack: ItemStack = e.result?:return
        val itemMeta: ItemMeta = itemStack.itemMeta ?: return

        val maxCustomDurability: Int = itemMeta.persistentDataContainer.get(
            BukkitConstants.MAX_CUSTOM_DURABILITY.value,
            BukkitConstants.MAX_CUSTOM_DURABILITY.dataType
        ) ?: return

        val damage: Short = itemStack.durability
        val empireDurability = maxCustomDurability - damage * maxCustomDurability / itemStack.type.maxDurability
        itemMeta.persistentDataContainer.set(
            BukkitConstants.EMPIRE_DURABILITY.value,
            BukkitConstants.EMPIRE_DURABILITY.dataType,
            empireDurability
        )
        itemStack.itemMeta = itemMeta
        val d: Int = itemStack.type.maxDurability -
                itemStack.type.maxDurability * empireDurability / maxCustomDurability
        itemStack.durability = d.toShort()
    }

    private fun changeDurability(itemStack: ItemStack?, damage: Int) {
        itemStack ?: return
        val itemMeta: ItemMeta = itemStack.itemMeta ?: return
        var maxCustomDurability: Int = itemMeta.persistentDataContainer.get(
            BukkitConstants.MAX_CUSTOM_DURABILITY.value,
            BukkitConstants.MAX_CUSTOM_DURABILITY.dataType
        ) ?: return

        var empireDurability: Int = itemMeta.persistentDataContainer.get(
            BukkitConstants.EMPIRE_DURABILITY.value,
            BukkitConstants.EMPIRE_DURABILITY.dataType
        ) ?: return



        empireDurability += damage

        if (empireDurability <= 0) {
            itemStack.durability = 0
        }

        if (empireDurability >= maxCustomDurability) {
            empireDurability = maxCustomDurability
        }

        itemMeta.persistentDataContainer.set(
            BukkitConstants.EMPIRE_DURABILITY.value,
            BukkitConstants.EMPIRE_DURABILITY.dataType,
            empireDurability
        )
        itemStack.itemMeta = itemMeta

        if (maxCustomDurability==0)
            maxCustomDurability = itemStack.type.maxDurability.toInt()
        val d: Int = itemStack.type.maxDurability -
                itemStack.type.maxDurability * empireDurability / maxCustomDurability
        itemStack.durability = d.toShort()

    }

    override fun onDisable() {
        PlayerItemMendEvent.getHandlerList().unregister(this)
        PlayerItemDamageEvent.getHandlerList().unregister(this)
        PrepareAnvilEvent.getHandlerList().unregister(this)
    }
}