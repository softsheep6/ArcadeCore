package me.softsheep6.arcadecore.games

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.abilities.Bendy
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable

class PassiveManager(private val plugin: ArcadeCore) : Listener {

    // stuff relating to passives. mostly involving effects/attributes and player hits

    // these first 3 handle permanent effects
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.player
        val game = GameManager(plugin).getGame(p)

        when (game) {
            Game.NONE -> return
            Game.BENDY -> Bendy(plugin).passiveA(p)
            // more
            else -> {}
        }
    }
    @EventHandler
    fun onPlayerEffectChange(e: EntityPotionEffectEvent) {
        val p = e.entity
        if (p !is Player) return
        if (e.cause != EntityPotionEffectEvent.Cause.MILK && e.cause != EntityPotionEffectEvent.Cause.TOTEM) return
        val game = GameManager(plugin).getGame(p)

        object : BukkitRunnable() {
            override fun run() {
                when (game) {
                    Game.NONE -> return
                    Game.BENDY -> Bendy(plugin).passiveA(p)
                    // more
                    else -> {}
                }
            }
        }.runTaskLater(plugin, 5L)
    }
    @EventHandler
    fun onPlayerRespawn(e: PlayerPostRespawnEvent) {
        val p = e.player
        val game = GameManager(plugin).getGame(p)

        when (game) {
            Game.NONE -> return
            Game.BENDY -> Bendy(plugin).passiveA(p)
            // more
            else -> {}
        }
    }


    // many abilities involve effects being applied after hitting/critting someone too, so this should go here as well
    @EventHandler
    fun onPlayerDamageByPlayer(e: EntityDamageByEntityEvent) {
        val p = e.entity
        val damager = e.damager
        if (p !is Player || damager !is Player) return
        val game = GameManager(plugin).getGame(p)

        // either the player or the damager can be passed into the passive methods, depending on if the effect is positive or negative
        when (game) {
            Game.NONE -> return
            Game.BENDY -> if (e.isCritical) Bendy(plugin).passiveB(damager)
            // more
            else -> {}
        }
    }
}