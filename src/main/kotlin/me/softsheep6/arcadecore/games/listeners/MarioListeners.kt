package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameUtils
import me.softsheep6.arcadecore.games.abilities.Mario
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class MarioListeners(private val plugin: ArcadeCore) : Listener {

    // worst passive of all time
    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val p = e.player
        val block = e.block
        // return if player who broke block doesn't have mario, or block broken wasn't an apple-dropping block
        if (GameUtils(plugin).getGame(p) != Game.MARIO || (block.type != Material.OAK_LEAVES && block.type != Material.DARK_OAK_LEAVES)) return

        // 10% chance for apple
        val random = (Math.random() * 10).toInt()
        if (random == 0) {
            e.isDropItems = false
            Mario(plugin).passiveB(p)
        }
    }
}