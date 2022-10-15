package com.astrainteractive.empire_items.empire_items.gui.crafting

import ru.astrainteractive.astralibs.async.PluginScope
import ru.astrainteractive.astralibs.events.EventManager
import ru.astrainteractive.astralibs.menu.AstraMenuSize
import ru.astrainteractive.astralibs.utils.convertHex
import com.astrainteractive.empire_items.api.EmpireItemsAPI.empireID
import com.astrainteractive.empire_items.api.EmpireItemsAPI.toAstraItemOrItem
import com.astrainteractive.empire_items.api.models.GUI_CONFIG
import com.astrainteractive.empire_items.api.utils.emoji
import com.astrainteractive.empire_items.empire_items.gui.GuiCategory
import com.astrainteractive.empire_items.empire_items.gui.PlayerMenuUtility
import com.astrainteractive.empire_items.empire_items.gui.toInventoryButton
import com.astrainteractive.empire_items.empire_items.util.EmpirePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.*
import ru.astrainteractive.astralibs.menu.PaginatedMenu

class GuiCrafting(playerMenuUtility: PlayerMenuUtility) : PaginatedMenu() {
    val viewModel = GuiCraftingViewModel(playerMenuUtility, playerMenuUtility.prevItems.last())

    val giveButtonIndex = 34
    val giveButton = GUI_CONFIG.settings.buttons.giveButton.toAstraItemOrItem()!!

    val recipeButtonIndex = 8
    val recipeButton: ItemStack?
        get() = when (viewModel.currentRecipeInfo?.type) {
            GuiCraftingViewModel.RecipeType.CRAFTING_TABLE -> GUI_CONFIG.settings.buttons.craftingTableButton.toAstraItemOrItem()!!
            GuiCraftingViewModel.RecipeType.FURNACE -> GUI_CONFIG.settings.buttons.furnaceButton.toAstraItemOrItem()!!
            else -> null
        }

    override var menuTitle: String = convertHex(
        GUI_CONFIG.settings.titles.workbenchText + (viewModel.itemID.toAstraItemOrItem()?.itemMeta?.displayName
            ?: "Крафтинг")
    ).emoji()


    override val menuSize: AstraMenuSize = AstraMenuSize.XL
    override val playerMenuUtility: PlayerMenuUtility = playerMenuUtility
    override val backPageButton = GUI_CONFIG.settings.buttons.backButton.toAstraItemOrItem()!!.toInventoryButton(49)
    override val maxItemsPerPage = 9
    override val maxItemsAmount: Int = viewModel.usedInCraftItemStacks.size
    override val nextPageButton = GUI_CONFIG.settings.buttons.nextButton.toAstraItemOrItem()!!.toInventoryButton(53)
    override var page: Int = playerMenuUtility.craftingPage
    override val prevPageButton = GUI_CONFIG.settings.buttons.prevButton.toAstraItemOrItem()!!.toInventoryButton(45)
    var currentRecipe = 0
    override fun loadPage(next: Int) {
        super.loadPage(next)
        playerMenuUtility.craftingPage += next
    }

    override fun onCreated() {
        setMenuItems()
    }


    override fun onInventoryClicked(e: InventoryClickEvent) {
        super.onInventoryClicked(e)
        when (e.slot) {
            backPageButton.index -> {
                PluginScope.launch {
                    playerMenuUtility.prevItems.removeLast()
                    if (playerMenuUtility.prevItems.isEmpty())
                        GuiCategory(playerMenuUtility).open()
                    else GuiCrafting(playerMenuUtility).open()
                }
            }

            recipeButtonIndex -> {
                viewModel.onRecipeIndexChanged()
                setMenuItems()
            }

            11, 12, 13, 20, 21, 22, 29, 30, 31 -> {
                val itemStack = inventory.getItem(e.slot)
                playerMenuUtility.prevItems.add(itemStack?.empireID ?: itemStack?.type?.name ?: return)
                lifecycleScope.launch(Dispatchers.IO) { GuiCrafting(playerMenuUtility).open() }
            }

            36, 37, 38, 39, 40, 41, 42, 43, 44 -> {
                val itemStack = inventory.getItem(e.slot)
                playerMenuUtility.prevItems.add(itemStack?.empireID ?: itemStack?.type?.name ?: return)
                lifecycleScope.launch(Dispatchers.IO) { GuiCrafting(playerMenuUtility).open() }
            }

            giveButtonIndex -> {
                if (playerMenuUtility.player.hasPermission(EmpirePermissions.EMPGIVE))
                    playerMenuUtility.player.inventory.addItem(viewModel.itemID.toAstraItemOrItem() ?: return)
            }
        }
    }

    override fun onInventoryClose(it: InventoryCloseEvent) {

    }

    override fun onPageChanged() {
        setMenuItems()
    }

    fun setMenuItems() {
        setManageButtons()
        inventory.setItem(backPageButton.index - 1, viewModel.dropInfo)
        inventory.setItem(backPageButton.index + 1, viewModel.blockInfo)
        inventory.setItem(backPageButton.index - 2, viewModel.villagersInfo)


        inventory.setItem(25, viewModel.itemID.toAstraItemOrItem())
        viewModel.currentRecipeInfo?.let {
            inventory.setItem(25, viewModel.itemID.toAstraItemOrItem()?.clone()?.apply {
                this.amount = it.amount
            })
            it.ingredients.mapIndexed { i, it ->
                val index = (i / 3 + 1) * 9 + 1 + i % 3 + 1
                inventory.setItem(index, it)
            }


        }
        for (i in 36 until 36 + 9) {
            val index = getIndex(i - 36)
            inventory.setItem(i, viewModel.usedInCraftItemStacks.getOrNull(index))
        }
        if (viewModel.recipes.isNotEmpty())
            inventory.setItem(recipeButtonIndex, recipeButton)
        inventory.setItem(giveButtonIndex, giveButton)


    }

}