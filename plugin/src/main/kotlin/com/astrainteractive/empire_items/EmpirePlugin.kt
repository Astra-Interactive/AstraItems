package com.astrainteractive.empire_items

import ru.astrainteractive.astralibs.AstraLibs
import ru.astrainteractive.astralibs.Logger
import ru.astrainteractive.astralibs.async.PluginScope
import ru.astrainteractive.astralibs.events.GlobalEventManager
import com.astrainteractive.empire_itemss.api.CraftingApi
import com.astrainteractive.empire_itemss.api.EmpireItemsAPI
import com.astrainteractive.empire_items.commands.CommandManager
import com.astrainteractive.empire_items.events.GenericListener
import com.astrainteractive.empire_items.meg.BossBarController
import com.astrainteractive.empire_items.meg.api.EmpireModelEngineAPI
import com.astrainteractive.empire_items.util.protection.KProtectionLib
import com.astrainteractive.empire_items.modules.ModuleManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File


class EmpirePlugin : JavaPlugin {
    constructor() : super() {}
    constructor(
        loader: JavaPluginLoader?,
        description: PluginDescriptionFile?,
        dataFolder: File?,
        file: File?,
    ) : super(
        loader!!, description!!, dataFolder!!, file!!
    )

    companion object {
        /**
         *Plugin instance
         */
        lateinit var instance: EmpirePlugin
            private set
    }
    init {
        instance = this
        AstraLibs.rememberPlugin(this)
    }

    /**
     * Command manager for plugin
     */
    private val commandManager by lazy { CommandManager() }


    /**
     * Generic listener event handler
     */
    private lateinit var genericListener: GenericListener

    private val modules = buildList {
        add(EmpireItemsAPI)
        add(ModuleManager)
        add(CraftingApi)
        add(EmpireModelEngineAPI)
        add(BossBarController)
    }



    /**
     * This function called when server starts
     */
    override fun onEnable() {
        Logger.prefix = "EmpireItems"
        commandManager
        genericListener = GenericListener()
        runBlocking { modules.forEach { it.onEnable() } }
        server.pluginManager.getPlugin("WorldGuard")?.let {
            KProtectionLib.init(this)
        }
    }


    /**
     * This function called when server stops
     */
    override fun onDisable() {
        PluginScope.cancel()
        genericListener.onDisable()
        for (p in server.onlinePlayers)
            p.closeInventory()

        GlobalEventManager.onDisable()
        HandlerList.unregisterAll(this)
        Bukkit.getScheduler().cancelTasks(this)
        runBlocking { modules.forEach { it.onDisable() } }
        PluginScope.cancel()
    }
}