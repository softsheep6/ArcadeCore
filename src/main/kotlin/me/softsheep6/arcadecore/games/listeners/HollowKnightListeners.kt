package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.games.listeners.HollowKnightListeners.Foo.player
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class HollowKnightListeners : Listener {

    object Foo {
        var player: Player? = null
    }

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.cause != EntityDamageEvent.DamageCause.FALL || e.entity !is Player || player == null) return

        // configurable
        val radius = 5.0 // in blocks, radius of damage/particles
        val damage = 6.0 // true damage
        val particleCount = 100
        val particleYOffset = 0.2

        // if the player who took fall damage is the same player who just used hk ability a,
        // then cancel the damage, do sfx and particles, and damage nearby untrusted players
        val damagedPlayer = e.entity as Player
        if (damagedPlayer == player) {
            e.isCancelled = true

            // sfx
            damagedPlayer.world.playSound(player!!.location, Sound.BLOCK_ANVIL_LAND, 1F, 0.5F)

            // particles
            Particle.BLOCK.builder()
                .location(damagedPlayer.location.clone().add(0.0, 1.0, 0.0))
                .data(BlockType.BLACKSTONE.createBlockData())
                .count(particleCount)
                .offset(radius/2, particleYOffset, radius/2)
                .spawn()
            Particle.DUST_PILLAR.builder()
                .location(damagedPlayer.location.clone().add(0.0, 1.0, 0.0))
                .data(BlockType.BLACKSTONE.createBlockData())
                .count(particleCount)
                .offset(radius/2, particleYOffset, radius/2)
                .spawn()


            // damage untrusted players
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
                }
            }
            player = null
        }
    }
}