package me.softsheep6.arcadecore.games.listeners

import io.papermc.paper.event.player.PlayerShieldDisableEvent
import me.softsheep6.arcadecore.games.listeners.CastleCrashersListeners.Foo.BREAKS
import me.softsheep6.arcadecore.games.listeners.CastleCrashersListeners.Foo.shieldBreaks
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CastleCrashersListeners : Listener {

    object Foo {
        val shieldBreaks = HashMap<Player, Int>()
        const val BREAKS = 3 // configurable
    }

    // only allow shield to be broken if it's already been broken BREAKS number of times, and then reset the count back to BREAKS again
    @EventHandler
    fun onShieldDisable(e: PlayerShieldDisableEvent) {
        val p = e.player

        if (shieldBreaks.contains(p)) {
            // subtract 1 from breaks, then reset count if 0, and cancel the break otherwise
            shieldBreaks[p] = (shieldBreaks[p]?.minus(1) ?: return)
            if (shieldBreaks[p] == 0) {
                shieldBreaks[p] = BREAKS
            }
            else e.isCancelled = true
        }
    }
}