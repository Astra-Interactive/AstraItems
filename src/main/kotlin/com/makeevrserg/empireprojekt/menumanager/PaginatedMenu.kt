package com.makeevrserg.empireprojekt.menumanager

import com.makeevrserg.empireprojekt.EmpirePlugin
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import com.makeevrserg.empireprojekt.util.Translations


abstract class PaginatedMenu(val translations:Translations,playerMenuUtility: PlayerMenuUtility?) : Menu(playerMenuUtility!!) {
    val plugin: EmpirePlugin = EmpirePlugin.plugin
    open var page = 0
    protected var maxItemsPerPage = 45
    var maxPages: Int = 1
    protected var index = 0


    fun checkFirstPage(): Boolean {
        if (page == 0) {
            playerMenuUtility.player
                .sendMessage(translations.FIRST_PAGE)
            return true
        }
        return false
    }

    fun reloadPage(next: Int) {
        page += next
        inventory.clear()
        setMenuItems()
    }

    fun checkLastPage(): Boolean {
        if (page >= maxPages) {
            playerMenuUtility.player
                .sendMessage(translations.LAST_PAGE)
            return true
        }
        return false
    }

    private fun setItem(page: String, items: MutableMap<String, ItemStack>, id: String?): ItemStack {

        id ?: return ItemStack(Material.PAPER)
        val itemStack = items[id] ?: ItemStack(Material.PAPER)
        val itemMeta = itemStack.itemMeta
        itemMeta.setDisplayName(page)
        itemStack.itemMeta = itemMeta

        return itemStack

    }


    fun addManageButtons() {
        if (page >= 1)
            inventory.setItem(
                45,
                setItem(
                    ChatColor.GREEN.toString() + "<- Пред. страница",
                    plugin.empireItems.empireItems,
                    plugin.empireFiles.guiFile.getConfig()?.getString("settings.prev_btn")
                )
            )

        inventory.setItem(
            49,
            setItem(
                ChatColor.GREEN.toString() + "Назад",
                plugin.empireItems.empireItems,
                plugin.empireFiles.guiFile.getConfig()?.getString("settings.back_btn")
            )
        )
        if (page < maxPages)
            inventory.setItem(
                53,
                setItem(
                    ChatColor.GREEN.toString() + "След. страница ->",
                    plugin.empireItems.empireItems,
                    plugin.empireFiles.guiFile.getConfig()?.getString("settings.next_btn")
                )
            )

    }
}
