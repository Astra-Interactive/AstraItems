package com.makeevrserg.empireprojekt

import com.makeevrserg.empireprojekt.ESSENTIALS.homes.EssentialsHandler
import com.makeevrserg.empireprojekt.NPCS.NPCManager
import com.makeevrserg.empireprojekt.commands.CommandManager
import com.makeevrserg.empireprojekt.util.CraftEvent
import com.makeevrserg.empireprojekt.events.GenericListener
import com.makeevrserg.empireprojekt.events.ItemUpgradeEvent
import com.makeevrserg.empireprojekt.events.genericevents.drop.ItemDropListener
import com.makeevrserg.empireprojekt.items.EmpireItems
import com.makeevrserg.empireprojekt.menumanager.emgui.settings.GuiCategories
import com.makeevrserg.empireprojekt.menumanager.emgui.settings.GuiSettings
import com.makeevrserg.empireprojekt.util.*
import com.makeevrserg.empireprojekt.util.files.Files
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin


class EmpirePlugin : JavaPlugin() {

    companion object {
        //Plugin instance
        lateinit var instance: EmpirePlugin
            private set

        //Files instance
        lateinit var empireFiles: Files
            private set

        //Items instance
        lateinit var empireItems: EmpireItems
            private set

        //Translations instance
        lateinit var translations: Translations
            private set

        //Config Instance
        lateinit var config: EmpireConfig
            private set

        //Font files instance aka custom hud and ui
        lateinit var empireFontImages: EmpireFontImages
            private set

        //Constants
        lateinit var empireConstants: EmpireConstats
            private set

        //Custom sounds instance
        lateinit var empireSounds: EmpireSounds
            private set

        //Npc manager instance
        var npcManager: NPCManager? = null
            private set
    }


    lateinit var _craftEvent: CraftEvent

    //Command manager for plugin
    private lateinit var commandManager: CommandManager

    //Handler for new home and warp mechanic
    private lateinit var essentialsHomesHandler: EssentialsHandler

    //private lateinit var npcManager:NPCManager
    private lateinit var genericListener: GenericListener

//    //Category section form emgui
//    lateinit var categoryItems: MutableMap<String, CategoryItems.CategorySection>

    //Drop from item
    lateinit var getEveryDrop: MutableMap<String, MutableList<ItemDropListener.ItemDrop>>

    //Recipies for items
    val recipies: MutableMap<String, CraftEvent.EmpireRecipe>
        get() = _craftEvent.empireRecipies

    //Upgrades for items
    val upgradesMap: MutableMap<String, List<ItemUpgradeEvent.ItemUpgrade>>
        get() = genericListener._itemUpgradeEvent.upgradesMap

    //GuiSettings
    lateinit var guiSettings: GuiSettings
    //Gui Categories
    lateinit var guiCategories: GuiCategories

    fun initPlugin() {
        empireConstants = EmpireConstats()
        translations = Translations()
        empireFiles = Files()
        EmpirePlugin.config = EmpireConfig.create()
        println(EmpirePlugin.config)
        empireSounds = EmpireSounds()
        empireFontImages = EmpireFontImages(empireFiles.fontImagesFile.getConfig())
        empireItems = EmpireItems()
        genericListener = GenericListener()
        commandManager = CommandManager()
        essentialsHomesHandler = EssentialsHandler()
//        categoryItems =
//            CategoryItems(
//                empireFiles.guiFile.getConfig()?.getConfigurationSection("categories")
//            ).categoriesMap
        getEveryDrop = genericListener._itemDropListener.everyDropByItem



        _craftEvent = CraftEvent()
        empireSounds.getSounds()

        guiSettings = GuiSettings()
        guiCategories = GuiCategories()
        if (server.pluginManager.getPlugin("ProtocolLib") != null) {
            npcManager = NPCManager()
        } else
            println(translations.PLUGIN_PROTOCOLLIB_NOT_INSTALLED)

        //Beta plugin countdown
        //PluginBetaAccessCheck()

    }


    override fun onEnable() {
        instance = this
        initPlugin()
    }

    fun disablePlugin() {

        fun isCustomRecipe(key: NamespacedKey): Boolean {
            return key.key.contains(empireConstants.CUSTOM_RECIPE_KEY)
        }

        fun isCustomRecipe(recipe: FurnaceRecipe): Boolean {
            return isCustomRecipe(recipe.key)
        }

        fun isCustomRecipe(recipe: ShapedRecipe): Boolean {
            return isCustomRecipe(recipe.key)
        }

        fun isCustomRecipe(recipe: Recipe): Boolean {
            if (recipe is FurnaceRecipe)
                return isCustomRecipe(recipe)
            else if (recipe is ShapedRecipe)
                return isCustomRecipe(recipe)
            return false
        }


        if (npcManager != null)
            npcManager!!.onDisable()
        genericListener.onDisable()
        server.scheduler.cancelTasks(this)
        val ite = server.recipeIterator()
        var recipe: Recipe?
        while (ite.hasNext()) {
            recipe = ite.next()
            if (isCustomRecipe(recipe)) {
                ite.remove()
                continue
            }
            val itemMeta = recipe?.result?.itemMeta ?: continue
            val id = itemMeta.persistentDataContainer.get(
                empireConstants.empireID,
                PersistentDataType.STRING
            ) ?: continue
            if (_craftEvent.empireRecipies.contains(id)) ite.remove()
        }


    }

    override fun onDisable() {
        disablePlugin()
        for (p in server.onlinePlayers)
            p.closeInventory()

    }
}