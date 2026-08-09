package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.MiscUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class Sonic(private val plugin: ArcadeCore) : AbstractGame() {
// KNOWN BUGS //
    /*
    -- none atm!
     */
    // TODO:
    //  n/a

    // cooldowns in seconds
    val abilityACD = 3
    val abilityBCD = 3

    override fun abilityA(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_A)) {
            p.sendMessage(Component.text("Ability A is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability A activated!").color(NamedTextColor.GREEN))

            // configurable
            val dur = 600 // duration of speed in ticks
            val amp = 2 // amplifier of speed
            val particleRadius = 0.25
            val particleCount = 100

            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, dur, amp))

            // sfx
            p.world.playSound(p.location, "entity.player.sonic1", 1f, 1f)

            // particles
            object : BukkitRunnable() {
                var index = 0
                override fun run() {

                    // particles
                    Particle.SPLASH.builder()
                        .location(p.location)
                        .offset(particleRadius, 0.0, particleRadius)
                        .count(particleCount)
                        .spawn()

                    index++
                    if (index >= dur) cancel()
                }
            }.runTaskTimer(plugin, 0L, 1L)

            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_A, abilityACD)
        }
    }

    override fun abilityB(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_B)) {
            p.sendMessage(Component.text("Ability B is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability B activated!").color(NamedTextColor.GREEN))

            // configurable
            val radius = 8.0 // in blocks, used to find nearest player
            val velocityMultiplier = 2.0 // whatever
            val damage = 6.0
            val damageRadius = 1.5 // in blocks, yea
            val dur = 40 // duration of particles in ticks
            val particleRadius = 0.25
            val particleCount = 20

            // get closest player
            val victim = MiscUtils().getNearestPlayer(p, radius) ?: return
            val direction = Vector(victim.x - p.x, (victim.y+.5) - p.y, victim.z - p.z).normalize()

            // velocity
            p.velocity = direction.multiply(velocityMultiplier)

            // sfx
            p.world.playSound(p.location, "entity.player.sonic2", 0.8f, 1f)

            // a lotta particles at the start
            Particle.INSTANT_EFFECT.builder()
                .location(p.location.clone().add(0.0, 1.0, 0.0))
                .offset(particleRadius, particleRadius*1.5, particleRadius)
                .color(50,100,255)
                .count(particleCount*10)
                .spawn()

            // This does things and you have to believe me because i am the one writing this comment and i do not lie
            object : BukkitRunnable() {
                var index = 0
                override fun run() {

                    // particles
                    Particle.INSTANT_EFFECT.builder()
                        .location(p.location.clone().add(0.0, 1.0, 0.0))
                        .offset(particleRadius, particleRadius*1.5, particleRadius)
                        .color(50,100,255)
                        .count(particleCount)
                        .spawn()

                    // if players are close enough, damage victim and big thing of particles
                    if (p.location.distance(victim.location) < damageRadius) {
                        victim.damage(damage)
                        Particle.INSTANT_EFFECT.builder()
                            .location(p.location.clone().add(0.0, 1.0, 0.0))
                            .offset(particleRadius*2, particleRadius*3, particleRadius*2)
                            .color(50,100,255)
                            .count(particleCount*4)
                            .spawn()
                        Particle.INSTANT_EFFECT.builder()
                            .location(p.location.clone().add(0.0, 0.0, 0.0))
                            .offset(particleRadius*5, particleRadius*8, particleRadius*5)
                            .color(10,20, 200)
                            .count(particleCount*10)
                            .spawn()
                        cancel()

                        //anddd sure have some y velocity that'd be cool
                        p.velocity = Vector(p.velocity.x, 0.5, p.velocity.z)

                    }

                    index++
                    if (index >= dur) cancel()
                }
            }.runTaskTimer(plugin, 0L, 1L)

            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 0

        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, dur, amp))
    }

    override fun passiveB(p: Player) {

        // configurable
        val dur = 20L // time in ticks to attempt to remove the sent block. will only succeed if distance between the sent block and the player is more than minDistance
        val cloud = Material.WHITE_STAINED_GLASS.createBlockData()
        val minDistance = 2.0 // minimum distance from the block to the player for the block to be removed

        // send block(s)
        val blockLoc = p.location
        blockLoc.y = 319.0
        p.sendBlockChange(blockLoc.clone().add(0.5,0.0,0.5), cloud)
        p.sendBlockChange(blockLoc.clone().add(-0.5,0.0,0.5), cloud)
        p.sendBlockChange(blockLoc.clone().add(0.5,0.0,-0.5), cloud)
        p.sendBlockChange(blockLoc.clone().add(-0.5,0.0,-0.5), cloud)

        // clear the block(s) (replace it with what it is on server side)
        object : BukkitRunnable() {
            override fun run() {
                // only clear block if the distance from the player's location (at y319) is more than minDistance blocks from the block's location
                if (Location(p.world, p.x, 319.0, p.z).distance(blockLoc.clone().add(0.5,0.0,0.5)) > minDistance) {
                    p.sendBlockChange(blockLoc.clone().add(0.5,0.0,0.5), blockLoc.clone().add(0.5,0.0,0.5).block.blockData)
                    cancel()
                }
                if (Location(p.world, p.x, 319.0, p.z).distance(blockLoc.clone().add(-0.5,0.0,0.5)) > minDistance) {
                    p.sendBlockChange(blockLoc.clone().add(-0.5,0.0,0.5), blockLoc.clone().add(-0.5,0.0,0.5).block.blockData)
                    cancel()
                }
                if (Location(p.world, p.x, 319.0, p.z).distance(blockLoc.clone().add(0.5,0.0,-0.5)) > minDistance) {
                    p.sendBlockChange(blockLoc.clone().add(0.5,0.0,-0.5), blockLoc.clone().add(0.5,0.0,-0.5).block.blockData)
                    cancel()
                }
                if (Location(p.world, p.x, 319.0, p.z).distance(blockLoc.clone().add(-0.5,0.0,-0.5)) > minDistance) {
                    p.sendBlockChange(blockLoc.clone().add(-0.5,0.0,-0.5), blockLoc.clone().add(-0.5,0.0,-0.5).block.blockData)
                    cancel()
                }
            }
        }.runTaskTimer(plugin, dur, dur)
    }
}