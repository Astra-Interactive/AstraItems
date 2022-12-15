package com.astrainteractive.empire_items.enchants.enchants

import com.astrainteractive.empire_items.enchants.core.EmpireEnchantApi
import com.astrainteractive.empire_items.enchants.core.EmpireEnchantEvent
import com.astrainteractive.empire_items.enchants.core.EmpireEnchants
import ru.astrainteractive.astralibs.async.PluginScope
import com.atrainteractive.empire_items.models.enchants.EmpireEnchantsConfig
import com.atrainteractive.empire_items.models.enchants.GenericValueEnchant
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent


class MegaJump(
    private val config: EmpireEnchantsConfig
) : EmpireEnchantEvent() {
    override val enchant = EmpireEnchants.MEGA_JUMP
    override val enchantKey = "Прыжок"
    override val materialWhitelist: List<Material>
        get() = EmpireEnchantApi.armorItems
    override val empireEnchant: GenericValueEnchant = config.enchants.MEGA_JUMP

    @EventHandler
    fun onJump(e: PlayerJumpEvent){

        val sum = e.player.inventory.armorContents?.mapNotNull {
            it?.let { getEnchantLevel(it) }?.times(empireEnchant.value)
        }?.sum()?:return
        PluginScope.launch {
            val total = empireEnchant.value
            if (e.player.isSneaking)
                e.player.velocity = e.to.subtract(e.from).toVector().multiply(sum)
        }
    }
    override fun onDisable() {
        EntityDamageByEntityEvent.getHandlerList().unregister(this)
    }

}