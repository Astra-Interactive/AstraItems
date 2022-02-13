package com.astrainteractive.empire_items.empire_items.api.drop

import com.astrainteractive.astralibs.Logger
import com.astrainteractive.empire_items.empire_items.api.items.data.ItemManager.toAstraItemOrItem
import com.astrainteractive.empire_items.empire_items.util.calcChance
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

object DropManager {

    private var dropsMap: MutableList<AstraDrop> = mutableListOf()

    fun clear() {
        dropsMap.clear()
    }

    fun loadDrops() {
        dropsMap = AstraDrop.getDrops().toMutableList()
        println(dropsMap)
    }

    fun getDropsFrom(dropFrom: String) = dropsMap.filter { it.dropFrom == dropFrom }.toSet().toMutableList()
    fun getDropsById(id: String) = dropsMap.filter { it.id == id }.toSet().toMutableList()


    fun getDrops(list:List<AstraDrop>): List<ItemStack> {
        return list.mapNotNull {
            if (!calcChance(it.chance))
                return@mapNotNull null
            val amount = Random(System.currentTimeMillis()).nextInt(it.minAmount, it.maxAmount + 1)
            if (amount <= 0)
                return@mapNotNull null
            return@mapNotNull it.id.toAstraItemOrItem(amount) ?: return@mapNotNull null
        }
    }

    fun spawnDrop(dropFrom: String, location: Location?): Boolean {
        var isDropped = false
        Logger.log("${dropFrom} ${getDropsFrom(dropFrom)}")
        getDropsFrom(dropFrom).forEach {
            if (!calcChance(it.chance))
                return@forEach
            val amount = Random(System.currentTimeMillis()).nextInt(it.minAmount, it.maxAmount + 1)
            if (amount <= 0)
                return@forEach
            val item = it.id.toAstraItemOrItem(amount) ?: return@forEach
            if (location?.world?.dropItemNaturally(location, item) != null)
                isDropped = true
        }
        return isDropped
    }

}