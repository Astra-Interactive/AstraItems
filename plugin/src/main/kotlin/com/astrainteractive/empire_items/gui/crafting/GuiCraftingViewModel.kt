package com.astrainteractive.empire_items.gui.crafting

import com.astrainteractive.empire_items.di.GuiConfigModule
import com.astrainteractive.empire_items.di.TranslationModule
import com.astrainteractive.empire_items.di.craftingApiModule
import com.astrainteractive.empire_items.di.empireItemsApiModule
import com.astrainteractive.empire_items.gui.PlayerMenuUtility
import com.astrainteractive.empire_items.util.ext_api.toAstraItemOrItem
import com.atrainteractive.empire_items.models.VillagerTradeInfo
import org.bukkit.ChatColor
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import ru.astrainteractive.astralibs.di.getValue
import ru.astrainteractive.astralibs.utils.HEX

class GuiCraftingViewModel(val playerMenuUtility: PlayerMenuUtility, val itemID: String) {
    private val translations by TranslationModule
    private val guiConfig by GuiConfigModule
    private val craftingApi by craftingApiModule
    private val empireItemsAPI by empireItemsApiModule
    /**
     * Предметы, которые можно сделать с помощью [itemID]
     */
    val recipes = listOf(craftingApi.recipesMap[itemID])

    /**
     * Крафты, в которых [itemID] используется
     */
    val usedInCraftIDS = craftingApi.usedInCraft(itemID)
    val usedInCraftItemStacks = usedInCraftIDS.map { it.toAstraItemOrItem() }
    private val drops = empireItemsAPI.dropsByID[itemID]
    private val generatedBlock = empireItemsAPI.itemYamlFilesByID[itemID]?.block?.generate
    private val villagerTrades = empireItemsAPI.villagerTradeInfoByID.values.mapNotNull {
        val filtered = it.trades.filter { it.value.id == itemID }.ifEmpty {
            return@mapNotNull null
        }
        VillagerTradeInfo(it.id, it.profession, filtered)
    }
    private var recipeInfoIndex = 0

    val dropInfo =
        if (drops.isNullOrEmpty()) null else guiConfig.settings.buttons.moreButton.toAstraItemOrItem()!!.apply {
            editMeta {
                it.setDisplayName((translations.guiInfoDropColor + translations.guiInfoDrop).HEX())
                it.lore =
                    drops.map { "${ChatColor.GRAY}${it.dropFrom}: [${it.minAmount};${it.maxAmount}] ${it.chance}%" }
            }
        }

    val blockInfo = generatedBlock?.let { b ->
        guiConfig.settings.buttons.moreButton.toAstraItemOrItem()!!.apply {
            editMeta {
                it.setDisplayName((translations.guiInfoDropColor + "Генерируется:").HEX())
                it.lore = listOf(
                    ("${ChatColor.GRAY}На высоте [${b.minY};${b.maxY}]"),
                    ("${ChatColor.GRAY}Количество в чанке [${b.minPerChunk};${b.maxPerChunk}]"),
                    ("${ChatColor.GRAY}Количество в депозите [${b.minPerDeposit};${b.maxPerDeposit}]"),
                    ("${ChatColor.GRAY}В мире: ${b.world ?: "любом"}"),
                    ("${ChatColor.GRAY}С шансом: ${b.generateInChunkChance}%")
                )
            }
        }
    }

    val villagersInfo =
        if (villagerTrades.isEmpty()) null else guiConfig.settings.buttons.moreButton.toAstraItemOrItem()!!.apply {
            editMeta {
                it.setDisplayName((translations.guiInfoDropColor + "Можно купить у жителя:").HEX())
                it.lore = villagerTrades.map { "${ChatColor.GRAY}${it.profession}" }
            }
        }
    private val recipesInfo = recipes.flatMap {
        it?.flatMap {
            listOfNotNull(
                (it as? ShapelessRecipe?)?.let(::fromShapelessRecipe),
                (it as? FurnaceRecipe?)?.let(::fromFurnaceRecipe),
                (it as? ShapedRecipe?)?.let(::fromShapedRecipe)
            )
        } ?: listOf()
    }


    fun onRecipeIndexChanged() {
        recipeInfoIndex++
    }

    val index: Int
        get() = kotlin.runCatching { recipeInfoIndex % recipesInfo.size }.getOrNull() ?: 0

    val currentRecipeInfo: RecipeInfo?
        get() = recipesInfo.getOrNull(index)


    enum class RecipeType {
        CRAFTING_TABLE, FURNACE
    }

    data class RecipeInfo(
        val amount: Int,
        val ingredients: List<ItemStack?>,
        val type: RecipeType
    )

    private fun ShapedRecipe.get(x: Int, y: Int): ItemStack? = ingredientMap[shape.getOrNull(x)?.getOrNull(y)]
    private fun fromShapedRecipe(it: ShapedRecipe): RecipeInfo {
        val range = IntRange(0, 2)
        val list = range.flatMap { x -> range.map { y -> it.get(x, y) } }
        return RecipeInfo(
            it.result.amount,
            list,
            RecipeType.CRAFTING_TABLE
        )

    }

    private fun fromFurnaceRecipe(it: FurnaceRecipe): RecipeInfo {
        val range = IntRange(0, 2)
        val list: MutableList<ItemStack?> = range.flatMap { x -> range.map { y -> null } }.toMutableList()
        list[4] = it.input
        return RecipeInfo(
            it.result.amount,
            list,
            RecipeType.FURNACE
        )
    }

    private fun fromShapelessRecipe(it: ShapelessRecipe): RecipeInfo {
        val range = IntRange(0, 8)
        val list: MutableList<ItemStack?> = range.map { x -> it.ingredientList.getOrNull(x) }.toMutableList()
        return RecipeInfo(
            it.result.amount,
            list,
            RecipeType.CRAFTING_TABLE
        )
    }
}