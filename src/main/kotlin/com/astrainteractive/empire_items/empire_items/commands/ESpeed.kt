package com.astrainteractive.empire_items.empire_items.commands

import ru.astrainteractive.astralibs.AstraLibs
import ru.astrainteractive.astralibs.Logger
import ru.astrainteractive.astralibs.utils.registerCommand
import com.astrainteractive.empire_items.EmpirePlugin
import com.astrainteractive.empire_items.ResourceProvider
import com.astrainteractive.empire_items.empire_items.util.EmpirePermissions
import com.astrainteractive.empire_items.empire_items.util.Translations
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*

private val translations: Translations
    get() = ResourceProvider.translations

/**
 * Reload command handler
 */
fun CommandManager.espeed() = AstraLibs.registerCommand("espeed") { sender, args ->
    if (!sender.hasPermission(EmpirePermissions.RELOAD)) {
        sender.sendMessage(translations.noPerms)
        return@registerCommand
    }
    if (sender!is Player) {
        Logger.warn("Player only command", tag = CommandManager.TAG)
        return@registerCommand
    }
    val p = sender as Player
    val speed = maxOf(0f,minOf(args.last().toFloatOrNull()?:10f,10f))/10f
    p.flySpeed = speed

}