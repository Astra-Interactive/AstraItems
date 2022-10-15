package com.astrainteractive.empire_items.empire_items.commands

import ru.astrainteractive.astralibs.AstraLibs
import ru.astrainteractive.astralibs.Logger
import ru.astrainteractive.astralibs.commands.AstraDSLCommand
import com.astrainteractive.empire_items.EmpirePlugin
import com.astrainteractive.empire_items.ResourceProvider
import com.astrainteractive.empire_items.api.EmpireItemsAPI.empireID
import com.astrainteractive.empire_items.api.EmpireItemsAPI.toAstraItem
import com.astrainteractive.empire_items.api.EmpireItemsAPI.toAstraItemOrItem
import com.astrainteractive.empire_items.empire_items.util.Translations
import org.bukkit.entity.Player
private val translations: Translations
    get() = ResourceProvider.translations
fun CommandManager.emReplace() = AstraDSLCommand.command("emreplace") {
    if (sender !is Player) {
        Logger.warn("Player only command", tag = CommandManager.TAG)
        return@command
    }

    val p = sender as Player
    var item = p.inventory.itemInMainHand
    val id = item.empireID ?: return@command
    val amount = item.amount
    val durability = item.durability
    item = id.toAstraItem(amount) ?: return@command
    item.durability = durability
    (sender as Player).inventory.setItemInMainHand(item)
    sender.sendMessage(translations.itemReplaced)
}
