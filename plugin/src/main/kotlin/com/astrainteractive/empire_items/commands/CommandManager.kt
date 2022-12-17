package com.astrainteractive.empire_items.commands

import com.astrainteractive.empire_items.EmpirePlugin
import com.astrainteractive.empire_items.di.GuiConfigModule
import com.astrainteractive.empire_items.di.empireUtilsModule
import com.astrainteractive.empire_items.di.fontApiModule
import ru.astrainteractive.astralibs.Logger
import ru.astrainteractive.astralibs.async.PluginScope
import ru.astrainteractive.astralibs.utils.convertHex
import ru.astrainteractive.astralibs.utils.then
import com.astrainteractive.empire_itemss.api.FontApi
import com.astrainteractive.empire_itemss.api.utils.EmpireUtils
import com.astrainteractive.empire_items.gui.GuiCategories
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.AstraLibs
import ru.astrainteractive.astralibs.di.getValue
import ru.astrainteractive.astralibs.utils.registerCommand

class CommandManager {
    companion object {
        const val TAG = "CommandManager"
    }

    init {
        emgui()
        emojiBook()
        AstraItemCommand()
        emReplace()
        espeed()
        reload()
        Ezip()
        General()
        ModelEngine()
        villagerInventory()
        villagerInventoryAutoComplete()
        AstraLibs.registerCommand("edisable") { sender, args ->
            EmpirePlugin.instance.apply {
                onDisable()
            }

        }

    }
}

fun CommandManager.emgui() = AstraLibs.registerCommand("emgui") {sender,args->
    val guiConfig by GuiConfigModule
    if (sender !is Player) {
        Logger.warn("Player only command", tag = CommandManager.TAG)
        return@registerCommand
    }
    PluginScope.launch {
        GuiCategories(sender, guiConfig = guiConfig).open()
    }
}

fun CommandManager.emojiBook() = AstraLibs.registerCommand("emojis") {sender,args->
    val fontApi by fontApiModule
    val empireUtils by empireUtilsModule
    if (sender !is Player) {
        Logger.warn("Player only command", tag = CommandManager.TAG)
        return@registerCommand
    }
    val list = fontApi.playerFonts().mapNotNull { (id, font) ->
        font.blockSend.then(null as String?) ?: convertHex("&r${font.id}\n&r&f${font.char}&r\n")
    }
    val book = empireUtils.getBook("RomaRoman", convertHex("&fЭмодзи"), listOf(list.joinToString(" ")), false)
    (sender as Player).openBook(book)

}