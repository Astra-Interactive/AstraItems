package com.astrainteractive.empire_items.commands

import com.astrainteractive.empire_items.di.TranslationModule
import com.astrainteractive.empire_items.di.empireItemsApiModule
import com.astrainteractive.empire_items.util.ext_api.toAstraItemOrItem
import com.astrainteractive.empire_items.plugin.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.AstraLibs
import ru.astrainteractive.astralibs.commands.registerCommand
import ru.astrainteractive.astralibs.commands.registerTabCompleter
import ru.astrainteractive.astralibs.di.getValue
import ru.astrainteractive.astralibs.utils.withEntry

class AstraItemCommand {

    private val translations by TranslationModule
    private val empireItemsAPI by empireItemsApiModule

    private val commandExecutor = AstraLibs.instance.registerCommand("emp") {
        val sender = this.sender
        if (!Permission.GiveCustomItem.hasPermission(sender)) {
            sender.sendMessage(translations.noPerms)
            return@registerCommand
        }
        if (sender !is Player) {
            sender.sendMessage(translations.notPlayer)
            return@registerCommand
        }
        if (args.getOrNull(0).equals("give", ignoreCase = true)) {
            val playerName = args.getOrNull(1)
            val id = args.getOrNull(2)
            val amount = args.getOrNull(3)?.toIntOrNull() ?: 1
            val itemStack = id.toAstraItemOrItem(amount) ?: run {
                sender.sendMessage(translations.itemNotExist)
                return@registerCommand
            }
            val player = playerName?.let(Bukkit::getPlayer) ?: run {
                sender.sendMessage(translations.playerNotFound)
                return@registerCommand
            }
            translations.itemGave(itemStack.itemMeta.displayName,player.name)
                .also(sender::sendMessage)

            translations.itemGained(itemStack.itemMeta.displayName)
                .also(sender::sendMessage)

            player.inventory.addItem(itemStack)
        }
    }
    private val tabCompleter = AstraLibs.instance.registerTabCompleter("emp") {
        when (args.size) {
            1 -> return@registerTabCompleter listOf("give").withEntry(args[0])
            2 -> return@registerTabCompleter Bukkit.getOnlinePlayers().map { it.name }.withEntry(args[1])
            3 -> return@registerTabCompleter empireItemsAPI.itemYamlFilesByID.keys.toList().withEntry(args[2])
        }
        return@registerTabCompleter listOf()

    }
}