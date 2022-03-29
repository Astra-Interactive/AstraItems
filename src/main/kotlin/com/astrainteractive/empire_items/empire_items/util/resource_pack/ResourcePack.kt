package com.astrainteractive.empire_items.empire_items.util.resource_pack

import com.astrainteractive.astralibs.AstraLibs
import com.astrainteractive.astralibs.Logger
import com.astrainteractive.astralibs.async.AsyncHelper
import com.astrainteractive.empire_items.api.font.FontApi
import com.astrainteractive.empire_items.api.items.BlockParser
import com.astrainteractive.empire_items.api.items.data.EmpireItem
import com.astrainteractive.empire_items.api.items.data.ItemApi
import com.astrainteractive.empire_items.api.sounds.AstraSounds
import com.astrainteractive.empire_items.empire_items.util.Timer
import com.astrainteractive.empire_items.empire_items.util.resource_pack.data.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Material
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ResourcePack {


    private fun generateFont() {
        val file = File(getFontPath() + File.separator + "default.json")
        if (file.exists())
            file.delete()
        file.createNewFile()
        val defaultFileText = InputStreamReader(AstraLibs.instance.getResource("pack/default.json")!!).readText()
        //saveFileFromResources("pack/negative_spaces.ttf", getFontPath() + File.separator + "negative_spaces.ttf")
        val providers = Gson().fromJson(defaultFileText, Providers::class.java)
        FontApi.allFonts().forEach {
            val p = Providers.Provider.fromAstraFont(it)
            providers.providers.add(p)
        }
        val json = Gson().toJson(providers)
        file.writeText(setPrettyString(json))
    }

    data class Sound(val sounds: List<String>) {
        constructor(sound: AstraSounds) : this(sound.sounds)
    }

    private fun generateSounds() {
        val sounds = AstraSounds.getSounds()
        val file = File(getAssetsFolder() + sep + sounds.first().namespace + sep + "sounds.json")
        val map = sounds.associate { it.id to Sound(it) }
        file.writeText(setPrettyString(Gson().toJson(map)))
        File(getMinecraftAssetsPath() + sep + "sounds.json").writeText(setPrettyString(Gson().toJson(map)))
    }


    private fun resolveParent(item: EmpireItem): String {
        val name = item.material.name
        return when {
            name.equals("BOW", ignoreCase = true) -> "minecraft:item/base/bow"
            name.contains("SHIELD") -> "minecraft:item/handheld"
            name.contains("CROSSBOW") -> "minecraft:item/generated"

            name.contains("HELMET") -> "minecraft:item/generated"
            name.contains("CHESTPLATE") -> "minecraft:item/generated"
            name.contains("BOOTS") -> "minecraft:item/generated"
            name.contains("LEGGINGS") -> "minecraft:item/generated"

            name.contains("HOE") -> "minecraft:item/handheld"
            name.contains("AXE") -> "minecraft:item/handheld"
            name.contains("PICKAXE") -> "minecraft:item/handheld"
            name.contains("SWORD") -> "minecraft:item/handheld"
            name.contains("SHOVEL") -> "minecraft:item/handheld"

            item.block != null -> "minecraft:block/base/block_real"

            else -> "minecraft:item/generated"
        }
    }

    private fun resolvePotion(item: EmpireItem) =
        if (item.material.name.contains("potion", ignoreCase = true)) "minecraft:item/potion" else null

    private fun createAutoGeneratedModel(item: EmpireItem) {
        if (item.customModelData == null || item.customModelData == 0)
            return
        if (item.texturePath == null && item.modelPath == null)
            return

        fun writeModel(file: String, model: Model) {
            val file = File(file)
            file.writeText(setPrettyString(Gson().toJson(model)))
        }

        val path = File(getAutoGeneratedPath() + File.separator).apply { mkdirs() }
        var model = Model(resolveParent(item), Textures(layer0 = "${item.namespace}:${item.texturePath}"))
        if (item.block != null) {
            model.textures?.layer0 = null
        }
        File(path.path + File.separator + "${item.id}.json").apply {
            delete()
            createNewFile()
        }
        writeModel(path.path + File.separator + "${item.id}.json", model)
        if (item.material == Material.BOW) {
            for (i in 0..2) {
                model = Model(resolveParent(item), Textures(layer0 = "${item.namespace}:${item.texturePath}_$i"))
                writeModel(path.path + File.separator + "${item.id}_$i.json", model)
            }
        }
    }

    fun createMinecraftModel(item: EmpireItem) {
        if (item.customModelData == null || item.customModelData == 0)
            return
        if (item.texturePath == null && item.modelPath == null)
            return

        fun resolveModel() =
            if (item.modelPath != null) "${item.namespace}:${item.modelPath}" else "minecraft:auto_generated/${item.id}"

        val file = File(getMinecraftItemsModelsPath() + sep + item.material.name.lowercase() + ".json")
        if (item.material == Material.BOW) {
            if (!file.exists())
                saveFileFromResources("pack/base/item/bow.json", getMinecraftItemsModelsPath() + sep + "bow.json")
        } else if (item.material == Material.SHIELD) {
            if (!file.exists()) {
                saveFileFromResources("pack/base/item/shield.json", getMinecraftItemsModelsPath() + sep + "shield.json")
                saveFileFromResources(
                    "pack/base/item/shield_blocking.json",
                    getMinecraftItemsModelsPath() + sep + "shield_blocking.json"
                )
            }
        } else if (item.material == Material.POTION) {
            if (!file.exists())
                saveFileFromResources("pack/base/item/potion.json", getMinecraftItemsModelsPath() + sep + "potion.json")
        }
        if (!file.exists())
            file.createNewFile()
        var model: Model? = Gson().fromJson(file.readText(), Model::class.java)
        if (model == null) {
            model = Model(
                if (item.block == null) resolveParent(item) else "minecraft:block/${item.material.name.lowercase()}",
                textures = Textures(
                    layer0 = "minecraft:item/${item.material.name.lowercase()}"
                )
            )
        }
        var predicate =
            Predicate(customModelData = item.customModelData, pulling = if (item.material == Material.BOW) 0 else null)
        var override = Override(model = resolveModel(), predicate = predicate)
        if (item.block != null) {
            model.textures = null
        }
        if (model.overrides == null) {
            model.overrides = listOf(override).toMutableList()
        } else
            model.overrides?.add(override)
        if (item.material == Material.SHIELD) {
            model.overrides?.removeLast()
            for (i in 0..1) {
                predicate = Predicate(customModelData = item.customModelData, blocking = i)
                override = Override(model = resolveModel(), predicate = predicate)
                model.overrides?.add(override)
            }
        }

        if (item.material == Material.BOW) {
            predicate = Predicate(customModelData = item.customModelData, pulling = 1)
            override = Override(model = resolveModel() + "_0", predicate = predicate)
            model.overrides?.add(override)
            predicate = Predicate(customModelData = item.customModelData, pulling = 1, pull = 0.65)
            override = Override(model = resolveModel() + "_1", predicate = predicate)
            model.overrides?.add(override)
            predicate = Predicate(customModelData = item.customModelData, pulling = 1, pull = 0.9)
            override = Override(model = resolveModel() + "_2", predicate = predicate)
            model.overrides?.add(override)

        }
        file.writeText((Gson().toJson(model)))


    }

    private fun generateItems() {
        File(getMinecraftItemsModelsPath()).delete()
        val simpleItems = ItemApi.getSimpleItems()
        val materials = simpleItems.forEach { item ->

            if (item.texturePath != null)
                createAutoGeneratedModel(item)
            createMinecraftModel(item)
        }
    }


    fun sortItems() {
        File(getMinecraftItemsModelsPath()).listFiles()?.forEach {
            if (!it.isFile)
                return@forEach
            val model: Model? = Gson().fromJson(it.readText(), Model::class.java) ?: return@forEach
            model?.overrides?.sortBy { it.predicate.customModelData }
            it.writeText(setPrettyString(Gson().toJson(model)))
        }
    }

    fun generateBlocks() {
        val blocks = ItemApi.getBlocksInfos()
        val existedData = blocks.map { it.block!!.data }
        for (i in 0..64 * 3 - 1) {
            if (existedData.contains(i))
                continue
            val materialName = BlockParser.getMaterialByData(i).name.lowercase()
            val multipart = Multipart(
                BlockParser.getFacingByData(i),
                Apply("minecraft:block/original/${materialName}_true")
            )
            File(getMinecraftAssetsPath() + sep + "blockstates" + sep).apply { mkdirs() }
            val file = File(getMinecraftAssetsPath() + sep + "blockstates" + sep + materialName + ".json")
            if (!file.exists())
                file.createNewFile()

            var blockModels = Gson().fromJson(file.readText(), BlockModel::class.java)
            if (blockModels == null)
                blockModels = BlockModel()
            blockModels.multipart.add(multipart)
            file.writeText(setPrettyString(Gson().toJson(blockModels)))
        }
        blocks.forEach { item ->
            val materialName = BlockParser.getMaterialByData(item.block?.data!!).name.lowercase()
            val path =
                if (item.modelPath != null) "${item.namespace}:${item.modelPath}" else "minecraft:auto_generated/${item.id}"
            val multipart = Multipart(
                BlockParser.getFacingByData(item.block.data),
                Apply(path)
            )
            val file = File(getMinecraftAssetsPath() + sep + "blockstates" + sep + materialName + ".json")
            if (!file.exists())
                file.createNewFile()
            var blockModels = Gson().fromJson(file.readText(), BlockModel::class.java)
            if (blockModels == null)
                blockModels = BlockModel()
            blockModels.multipart.add(multipart)
            file.writeText(setPrettyString(Gson().toJson(blockModels)))


        }

    }

    private fun deleteFilesInFolder(folder: String) =
        File(folder).listFiles()?.forEach { it.delete() }

    private fun deleteFolder(folder: String) = File(folder).delete()
    private fun createDirectory(folder: String) = File(folder).mkdirs()
    private fun recreateDirectory(path: String) {
        deleteFilesInFolder(path)
        deleteFolder(path)
        createDirectory(path)
    }


    init {
        val minecraftPath = getMinecraftAssetsPath()
        deleteFilesInFolder("$minecraftPath/models/item")

        createDirectory(getAssetsFolder())
        createDirectory(minecraftPath)
        recreateDirectory(getAutoGeneratedPath())
        //blockstates
        var path = "$minecraftPath${sep}blockstates"
        recreateDirectory(path)
        //font
        path = "$minecraftPath${sep}font"
        createDirectory(path)
        //lang
        path = "$minecraftPath${sep}lang"
        createDirectory(path)
        //models
        path = "$minecraftPath${sep}models${sep}item${sep}base"
        recreateDirectory(path)
        saveFileFromResources("pack/base/item/bow.json", path + sep + "bow.json")
        saveFileFromResources("pack/base/item/shield.json", path + sep + "shield.json")
        saveFileFromResources("pack/base/item/shield_blocking.json", path + sep + "shield_blocking.json")

        path = "$minecraftPath${sep}models${sep}block${sep}base"
        recreateDirectory(path)
        saveFileFromResources("pack/base/block/base/block_real.json", path + sep + "block_real.json")

        path = "$minecraftPath${sep}models${sep}block${sep}original"
        recreateDirectory(path)

        saveFileFromResources(
            "pack/base/block/original/brown_mushroom_block_true.json",
            path + sep + "brown_mushroom_block_true.json"
        )
        saveFileFromResources(
            "pack/base/block/original/mushroom_stem_true.json",
            path + sep + "mushroom_stem_true.json"
        )
        saveFileFromResources(
            "pack/base/block/original/red_mushroom_block_true.json",
            path + sep + "red_mushroom_block_true.json"
        )
        //shaders
        path = "$minecraftPath${sep}shaders${sep}core"
        recreateDirectory(path)
        saveFileFromResources(
            "pack/base/shaders/core/rendertype_armor_cutout_no_cull.json",
            path + sep + "rendertype_armor_cutout_no_cull.json"
        )
        saveFileFromResources(
            "pack/base/shaders/core/rendertype_armor_cutout_no_cull.fsh",
            path + sep + "rendertype_armor_cutout_no_cull.fsh"
        )
        saveFileFromResources(
            "pack/base/shaders/core/rendertype_armor_cutout_no_cull.vsh",
            path + sep + "rendertype_armor_cutout_no_cull.vsh"
        )
        //texts
        createDirectory("$minecraftPath${sep}texts")


        Timer().calculate {
            generateFont()
        }.also {
            Logger.log("generateFont completed it ${it}")
        }
        Timer().calculate {
            generateSounds()
        }.also {
            Logger.log("generateSounds completed it ${it}")
        }
        Timer().calculate {
            runBlocking {
                generateItems()
            }
        }.also {
            Logger.log("generateItems completed it ${it}")
        }
        Timer().calculate {
            sortItems()
        }.also {
            Logger.log("sortItems completed it ${it}")
        }
        Timer().calculate {
            generateBlocks()
        }.also {
            Logger.log("generateBlocks completed it ${it}")
        }

    }

    companion object {
        val sep: String
            get() = File.separator

        fun generate() = ResourcePack()
        private fun setPrettyString(line: String): String =
            GsonBuilder().setPrettyPrinting().create().toJson(JsonParser().parse(line).asJsonObject)
                .replace("\\\\", "\\")

        private fun getAutoGeneratedPath() =
            getAssetsFolder() + File.separator + "minecraft" + File.separator + "models" + File.separator + "auto_generated"

        /**
         * Возвращает путь <plugin>/pack/assets
         */
        private fun getAssetsFolder() =
            AstraLibs.instance.dataFolder.toString() +
                    File.separator + "pack" +
                    File.separator + "assets"

        /**
         * Возвращает путь AstraItems/pack/assets/minecraft
         */
        private fun getMinecraftAssetsPath(): String = getAssetsFolder() +
                File.separator + "minecraft"

        /**
         * Возвращает путь AstraItems/pack/assets/minecraft/models
         */
        private fun getMinecraftModelsPath(): String = getMinecraftAssetsPath() +
                File.separator + "models"

        /**
         * Возвращает путь AstraItems/pack/assets/minecraft/models/item
         */
        private fun getMinecraftItemsModelsPath(): String = getMinecraftModelsPath() + File.separator + "item"

        /**
         * Возвращает путь AstraItems/pack/assets/minecraft/block
         */
        private fun getMinecraftBlocksModelsPath(): String = getMinecraftModelsPath() + File.separator + "block"

        /**
         * Возвращает путь AstraItems/pack/assets/minecraft/font
         */
        private fun getFontPath() = getMinecraftAssetsPath() + File.separator + "font"

        private fun saveFileFromResources(path: String, outputPath: String, replace: Boolean = true): Boolean {
            val option = if (replace) StandardCopyOption.REPLACE_EXISTING else null
            val input = AstraLibs.instance.getResource(path)
            val output = File(outputPath).apply { createNewFile() }
            Files.copy(
                input ?: return false,
                output.toPath(),
                option
            )
            return true
        }

    }
}