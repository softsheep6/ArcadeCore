package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.games.listeners.ValorantListeners.Foo.activeAbilities
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class ValorantListeners : Listener {

    object Foo {
        val activeAbilities = HashMap<Player, Int>() //look im using a map im So fancy
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.action != Action.LEFT_CLICK_AIR || e.action != Action.LEFT_CLICK_BLOCK) return
        if (!activeAbilities.contains(e.player)) return // return if player who interacted isn't using ability

        val p = e.player
        val arrow = p.world.spawnArrow(p.location, p.location.direction, 1f, 0f)

        // decrease swords by 1
        activeAbilities[p] = activeAbilities[p]?.minus(1) ?: 0 // wtf is .minus ive never seen that ever ever whatever thanks intellij i guess


    }
}