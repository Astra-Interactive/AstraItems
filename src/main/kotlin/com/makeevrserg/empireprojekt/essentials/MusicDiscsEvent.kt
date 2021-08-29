package com.makeevrserg.empireprojekt.essentials

import com.makeevrserg.empireprojekt.EmpirePlugin
import com.makeevrserg.empireprojekt.empirelibs.EmpireUtils
import com.makeevrserg.empireprojekt.empirelibs.IEmpireListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.block.Jukebox
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class MusicDiscsEvent : IEmpireListener {

    private val musicDiscs = EmpirePlugin.empireItems.empireDiscsEvent
    private val activDiscs = mutableMapOf<Location, CompanionDisc>()


    class MusicDisc {
        lateinit var song: String
        fun create(itemSection: ConfigurationSection): MusicDisc? {
            song = itemSection.getString("song") ?: return null
            return this
        }
        fun create(song:String?): MusicDisc? {
            this.song = song ?: return null
            return this
        }
    }

    data class CompanionDisc(val musicSoundKey: String, val id: String, val players: List<Player>)


    private fun startPlaying(item: ItemStack, location: Location): Boolean {
        if (activDiscs.containsKey(location))
            return false
        val musicDiscID = EmpireUtils.getEmpireID(item) ?: return false
        Bukkit.getServer()
        item.amount -= 1
        val musicDisc = musicDiscs[musicDiscID] ?: return false

        //val key = Key.key(musicDisc.song)
        activDiscs[location] = CompanionDisc(musicDisc.song, musicDiscID, getPlayerInDistance(location, 16 * 4))
        //val sound = Sound.sound(key, Sound.Source.RECORD, 4.0f, 1.0f)

        for (player in getPlayerInDistance(location,16*4))
            player.playSound(location,musicDisc.song,SoundCategory.RECORDS,4.0f,1.0f)


        //location.world?.playSound(location,musicDisc.song,4.0f,4.0f)?:return false
        return true
    }

    private fun getPlayerInDistance(location: Location, dist: Int): MutableList<Player> {
        val list = mutableListOf<Player>()
        for (p in location.world?.players?:return mutableListOf())
            if (p.location.distance(location) <= dist)
                list.add(p)
        return list

    }

    private fun stopPlaying(location: Location): Boolean {
        val disc = activDiscs[location] ?: return false
        activDiscs.remove(location)
        for (p in disc.players)
            if (p.isOnline)
                p.stopSound(disc.musicSoundKey, SoundCategory.RECORDS)

        location.world?.dropItem(location.add(0.0, 1.0, 0.0), EmpirePlugin.empireItems.empireItems[disc.id] ?: return false)?:return false
        location.world?.playSound(location, org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f)?:return false
        return true
    }

    private fun isJukebox(e: PlayerInteractEvent): Jukebox? {
        val block = e.clickedBlock ?: return null
        if (block.type != Material.JUKEBOX || e.action != Action.RIGHT_CLICK_BLOCK)
            return null
        val blockState = block.state
        if (blockState !is Jukebox)
            return null
        return blockState
    }


    @EventHandler
    fun blockExplodeEvent(e: BlockExplodeEvent) {

        for (block in e.blockList())
            if (block.type == Material.JUKEBOX)
                stopPlaying(block.location)
    }

    @EventHandler
    fun playerBlockBreak(e: BlockBreakEvent) {
        val location = e.block.location
        stopPlaying(location)
    }

    private fun isEmpireDisk(item:ItemStack): Boolean {
        val id = EmpireUtils.getEmpireID(item)?:return false
        musicDiscs[id]?:return false
        return true
    }

    @EventHandler
    fun onJukeboxInteract(e: PlayerInteractEvent) {
        val jukebox = isJukebox(e) ?: return
        val location = jukebox.location
        if (!isEmpireDisk(e.player.inventory.itemInMainHand)) {
            stopPlaying(location)
            return
        }
        if (jukebox.isPlaying) {
            jukebox.eject()
            return
        }

        e.isCancelled = true
        if (stopPlaying(location))
            return

        startPlaying(e.player.inventory.itemInMainHand, location)


    }



    override fun onDisable() {
        PlayerInteractEvent.getHandlerList().unregister(this)
        BlockBreakEvent.getHandlerList().unregister(this)
        BlockExplodeEvent.getHandlerList().unregister(this)
        for (key in activDiscs.keys) {
            //key.world.stopSound(SoundStop.named(activeDiscd[key]!!.musicSoundKey))
            stopPlaying(key)
        }

    }
}