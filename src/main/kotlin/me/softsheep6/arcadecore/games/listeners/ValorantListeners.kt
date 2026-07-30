package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.games.listeners.ValorantListeners.Foo.abilityActivePlayers
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class ValorantListeners : Listener {

    object Foo {
        val abilityActivePlayers = ArrayList<Player>()
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.action != Action.LEFT_CLICK_AIR || e.action != Action.LEFT_CLICK_BLOCK) return
        if (!abilityActivePlayers.contains(e.player)) return // return if player who interacted isn't using ability

        val p = e.player
        val arrow = p.world.spawnArrow(p.location, p.location.direction, 1f, 0f)

    }
}