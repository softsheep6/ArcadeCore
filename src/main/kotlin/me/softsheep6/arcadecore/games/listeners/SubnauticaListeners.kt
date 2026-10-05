package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.games.listeners.SubnauticaListeners.Foo.waterOrigins
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent

class SubnauticaListeners : Listener {

    object Foo {
        val waterOrigins: ArrayList<Location> = ArrayList()
    }

    // cancel flowing water if it's ability water, or if it's nearby ability water
    @EventHandler
    fun onBlockFromTo(e: BlockFromToEvent) {

        // return if not water
        if (e.block.type != Material.WATER) return

        for (loc in waterOrigins) {
            if (loc.world == e.block.location.world && loc.distance(e.block.location) < 6.0) e.isCancelled = true
        }

    }

    // prevent obsidian/stone/cobblestone forming with the ability water
    @EventHandler
    fun onBlockForm (e: BlockFormEvent) {

        // return if not water
        if (e.block.type != Material.WATER) return

        for (loc in waterOrigins) {
            if (loc.world == e.block.location.world && loc.distance(e.block.location) < 6.0) e.isCancelled = true
        }
    }

}