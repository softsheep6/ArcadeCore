package me.softsheep6.arcadecore.games.listeners

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.entity.Guardian
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PokemonListeners : Listener {

    @EventHandler
    fun onPlayerAttack (e: PrePlayerAttackEntityEvent) {
        if (e.attacked is Guardian && e.attacked.isInvulnerable) e.isCancelled = true
    }
}