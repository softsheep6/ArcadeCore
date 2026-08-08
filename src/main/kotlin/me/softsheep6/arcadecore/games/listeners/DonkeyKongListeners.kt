package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameUtils
import me.softsheep6.arcadecore.games.StunManager
import me.softsheep6.arcadecore.games.listeners.DonkeyKongListeners.Foo.players
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockType
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class DonkeyKongListeners(private val plugin: ArcadeCore) : Listener {

    object Foo {
        var players = HashSet<Player>()
    }

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.cause != EntityDamageEvent.DamageCause.FALL || e.entity !is Player || players.isEmpty()) return

        // configurable
        val radius = 4.0 // in blocks, radius of damage/particles
        val damage = 6.0 // true damage
        val stunDur = 30L // duration of stun, in ticks
        val particleCount = 200
        val particleYOffset = 0.2

        // if the player who took fall damage is the same player who just used hk ability a,
        // then cancel the damage, do sfx and particles, and damage nearby untrusted players
        val damagedPlayer = e.entity as Player
        if (players.contains(damagedPlayer)) {
            e.isCancelled = true

            // sfx
            damagedPlayer.world.playSound(damagedPlayer.location, Sound.ENTITY_HORSE_LAND, 1F, 0.7F)

            // particles
            Particle.BLOCK.builder()
                .location(damagedPlayer.location.clone().add(0.0, 1.0, 0.0))
                .data(BlockType.STRIPPED_OAK_LOG.createBlockData())
                .count(particleCount/2)
                .offset(radius/2, particleYOffset, radius/2)
                .spawn()
            Particle.DUST_PILLAR.builder()
                .location(damagedPlayer.location.clone().add(0.0, 1.0, 0.0))
                .data(BlockType.STRIPPED_OAK_LOG.createBlockData())
                .count(particleCount)
                .offset(radius/2, particleYOffset, radius/2)
                .spawn()


            // damage & stun untrusted players
            val nearbyPlayers = damagedPlayer.world.getNearbyPlayers(damagedPlayer.location, radius)
            nearbyPlayers.remove(damagedPlayer)
            // remove trusted from list here
            if (nearbyPlayers.isEmpty()) return
            else {
                nearbyPlayers.forEach {
                    // deals true damage. if the player's current health minus the damage dealt would kill them, then deal a ton of damage instead so that kill credit is shown.
                    // otherwise just do the damage
                    // also do a really tiny bit of damage regardless for the purpose of iframes and all that
                    it.damage(0.01, damagedPlayer)
                    if (it.health - damage < 0.0) {
                        it.health = 0.001
                        it.damage(10.0, damagedPlayer)
                    } else it.health -= damage

                    // & stun them
                    StunManager(plugin).stunPlayer(it, stunDur)
                }
            }
            players.remove(damagedPlayer)
        }
    }

    // arrow damage immunity
    @EventHandler
    fun onPlayerDamage(e: EntityDamageByEntityEvent) {
        if (e.entity !is Player) return
        val p = e.entity as Player
        if (e.damager is Arrow && GameUtils(plugin).getGame(p) == Game.DONKEY_KONG) e.isCancelled = true
    }
}