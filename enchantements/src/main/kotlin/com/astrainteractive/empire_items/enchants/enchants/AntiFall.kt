package com.astrainteractive.empire_items.enchants.enchants

import com.astrainteractive.empire_items.enchants.core.EmpireEnchantApi
import com.astrainteractive.empire_items.enchants.core.EmpireEnchantEvent
import com.astrainteractive.empire_items.models.bukkit.EmpireEnchants
import com.atrainteractive.empire_items.models.enchants.EmpireEnchantsConfig
import com.atrainteractive.empire_items.models.enchants.GenericValueEnchant
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import ru.astrainteractive.astralibs.di.IDependency
import ru.astrainteractive.astralibs.di.getValue


class AntiFall(configModule: IDependency<EmpireEnchantsConfig>): EmpireEnchantEvent() {
    override val enchant = EmpireEnchants.ANTI_FALL
    override val enchantKey = "Гравитин"
    private val config: EmpireEnchantsConfig by configModule
    override val materialWhitelist: List<Material>
        get() = EmpireEnchantApi.armorItems
    override val empireEnchant: GenericValueEnchant = config.enchants.ANTI_FALL

    @EventHandler
    private fun onEntityFall(e: EntityDamageEvent) {
        if (e.cause != EntityDamageEvent.DamageCause.FALL) return
        val player = (e.entity as? Player) ?: return
        val list = player.inventory.armorContents?.mapNotNull {
            it ?: return@mapNotNull null
            getEnchantLevel(it)
        }
        if (list.isNullOrEmpty()) return
        list.forEach {
            val total = if (empireEnchant.value < 1) 1 / it else it
            e.damage *= empireEnchant.value * total
        }

    }

    override fun onDisable() {
        EntityDamageByEntityEvent.getHandlerList().unregister(this)
    }

}
