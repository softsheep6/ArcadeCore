package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameUtils
import me.softsheep6.arcadecore.games.abilities.Sonic
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class SonicListeners(private val plugin: ArcadeCore) : Listener {

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        val p = e.player
        if (p.y < 320 || GameUtils(plugin).getGame(p) != Game.SONIC) return
        Sonic(plugin).passiveB(p)
    }
}