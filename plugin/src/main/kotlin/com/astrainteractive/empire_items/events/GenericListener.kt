package com.astrainteractive.empire_items.events


import com.astrainteractive.empire_items.events.blocks.BlockGenerationEvent
import com.astrainteractive.empire_items.events.blocks.MushroomBlockBreakEvent
import com.astrainteractive.empire_items.events.blocks.MushroomBlockPlaceEvent
import com.astrainteractive.empire_items.events.blocks.MushroomCancelEvent
import com.astrainteractive.empire_items.events.empireevents.*
import com.astrainteractive.empire_items.events.empireevents.hammer.HammerEvent
import com.astrainteractive.empire_items.events.genericevents.*
import com.astrainteractive.empire_items.events.resourcepack.ProtocolLibResourcePackEvent
import com.astrainteractive.empire_items.events.resourcepack.ResourcePackEvent
import com.astrainteractive.empire_items.api.utils.getPlugin
import ru.astrainteractive.astralibs.events.EventListener
import ru.astrainteractive.astralibs.events.EventManager


class GenericListener : EventManager {
    override val handlers: MutableList<EventListener> = mutableListOf()


    val blocksEventModule = {
        BlockGenerationEvent()
//        BlockHardnessEvent()
        MushroomBlockBreakEvent()
        MushroomBlockPlaceEvent()
        MushroomCancelEvent()
//        TestMushroomEvent()
    }
    val empireEvents = {
//        LavaWalkerEvent()
        HammerEvent()
        MolotovEvent()
        MusicDiscsEvent().onEnable(this)
        SlimeCatchEvent()
        DeathTotemEvent()
        SoulBindEvent()
        GrenadeEvent()
        GrapplingHook()
        VoidTotemEvent()
        GunEvent()
        getPlugin("CoreProtect")?.let {
//            CoreInspectEvent()
        }
    }
    val genericEvents = {
        ExperienceRepairEvent()
        ItemDropEvent()
        ItemInteractEvent()
        VillagerEvent()
        PlayerShowRecipeKeyEvent()
        ResourcePackEvent()
        BookSignEvent().onEnable(this)

    }
    val apiEvents = {
        getPlugin("ProtocolLib")?.let {
//            FontProtocolLibEvent().onEnable(this)

            ProtocolLibResourcePackEvent().onEnable(this)
        }
        getPlugin("ModelEngine")?.let {
//            ModelEngineEvent().onEnable(this)
//            ModelEngineEvent()
        }
//        DecorationEvent()
    }

    override fun onDisable() {
        super.onDisable()
//        PlibFontListener.onDisable()


    }
    init {
        blocksEventModule()
        empireEvents()
        genericEvents()
        apiEvents()
//        PlibFontListener.onEnable()
    }
}

