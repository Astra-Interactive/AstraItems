package com.astrainteractive.empire_items.enchants.enchants

import com.astrainteractive.empire_items.enchants.calcChance
import com.astrainteractive.empire_items.enchants.core.EmpireEnchantEvent
import com.astrainteractive.empire_items.models.bukkit.EmpireEnchants
import com.atrainteractive.empire_items.models.enchants.EmpireEnchantsConfig
import com.atrainteractive.empire_items.models.enchants.GenericValueEnchant
import org.bukkit.Material
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import ru.astrainteractive.astralibs.di.IDependency
import ru.astrainteractive.astralibs.di.getValue


class Vyderlight(configModule: IDependency<EmpireEnchantsConfig>): EmpireEnchantEvent() {
    override val enchant = EmpireEnchants.VYDERLIGHT
    override val enchantKey = "Свет Богатсва"
    private val config: EmpireEnchantsConfig by configModule
    override val materialWhitelist: List<Material>
        get() = listOf(
            Material.NETHERITE_SWORD,
            Material.DIAMOND_SWORD,
            Material.STONE_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.WOODEN_SWORD
        )
    override val empireEnchant: GenericValueEnchant = config.enchants.VYDERLIGHT


    @EventHandler
    private fun onEntityDamage(e: EntityDamageByEntityEvent) {
        if (e.damager !is Player) return
        if (e.entity is Monster)
            return
        val p = e.damager as Player
        val level = getEnchantLevel(p.inventory.itemInMainHand) ?: return
        if (calcChance(20))
            p.damage(level * empireEnchant.value * e.damage)
        if (calcChance(20))
            p.location.world.strikeLightning(p.location)
        if (calcChance(10))
            e.damage = 0.0
    }

    override fun onDisable() {
        EntityDamageByEntityEvent.getHandlerList().unregister(this)
    }

}
