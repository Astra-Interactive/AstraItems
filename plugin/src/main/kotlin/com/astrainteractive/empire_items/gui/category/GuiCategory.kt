package com.astrainteractive.empire_items.gui.category

import com.astrainteractive.empire_items.di.GuiConfigModule
import com.astrainteractive.empire_items.gui.PlayerMenuUtility
import com.astrainteractive.empire_items.gui.categories.GuiCategories
import com.astrainteractive.empire_items.gui.crafting.GuiCrafting
import com.astrainteractive.empire_items.util.ext_api.toAstraItemOrItem
import com.astrainteractive.empire_items.api.utils.emoji
import com.astrainteractive.empire_items.util.toInventoryButton
import com.atrainteractive.empire_items.models.config.GuiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import ru.astrainteractive.astralibs.async.PluginScope
import ru.astrainteractive.astralibs.menu.MenuSize
import ru.astrainteractive.astralibs.menu.PaginatedMenu
import ru.astrainteractive.astralibs.utils.convertHex

class GuiCategory(override val playerMenuUtility: PlayerMenuUtility, private val guiConfig: GuiConfig) : PaginatedMenu() {

    val category = guiConfig.categories[playerMenuUtility.categoryId]!!

    override var menuTitle: String = convertHex(category.title).emoji()

    override val menuSize: MenuSize = MenuSize.XL
    override val backPageButton = guiConfig.settings.buttons.backButton.toAstraItemOrItem()!!.toInventoryButton(49)
    override val maxItemsAmount: Int = category.items.size
    override val nextPageButton = guiConfig.settings.buttons.nextButton.toAstraItemOrItem()!!.toInventoryButton(53)
    override var page: Int = playerMenuUtility.categoryPage

    override val prevPageButton = guiConfig.settings.buttons.prevButton.toAstraItemOrItem()!!.toInventoryButton(45)


    override fun loadPage(next: Int) {
        super.loadPage(next)
        playerMenuUtility.categoryPage += next
    }

    override fun onInventoryClicked(e: InventoryClickEvent) {
        e.isCancelled = true
        handleChangePageClick(e.slot)
        when (e.slot) {
            backPageButton.index -> {
                PluginScope.launch(Dispatchers.IO) {
                    GuiCategories(playerMenuUtility.player, guiConfig = GuiConfigModule.value).open()
                }
            }

            prevPageButton.index, nextPageButton.index -> {

            }

            else -> {
                val index = getIndex(e.slot)
                PluginScope.launch(Dispatchers.IO) {
                    playerMenuUtility.prevItems.add(category.items.getOrNull(index)?:return@launch)
                    GuiCrafting(playerMenuUtility, GuiConfigModule.value).open()
                }
            }
        }
    }

    override fun onCreated() {
        setMenuItems()
    }

    override fun onInventoryClose(it: InventoryCloseEvent) {
        close()
    }
    override fun onPageChanged() {
        setMenuItems()
    }

    fun setMenuItems() {
        inventory.clear()
        setManageButtons()
        val items = guiConfig.categories.values ?: return
        for (i in 0 until maxItemsPerPage) {
            val index = getIndex(i)
            inventory.setItem(i, category.items.getOrNull(index)?.toAstraItemOrItem() ?: continue)
        }

    }

}