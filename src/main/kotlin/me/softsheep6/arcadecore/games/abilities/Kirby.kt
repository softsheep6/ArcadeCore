package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.MiscUtils
import me.softsheep6.arcadecore.games.listeners.KirbyListeners
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class Kirby(private val plugin: ArcadeCore) : AbstractGame() {

    // KNOWN BUGS //
    /*
    -- might wanna change this, ability A currently prevents mace from working, due to constantly resetting fall distance.
        could be good from a balance standpoint but was unintentional
    --
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
            val jumps = 3
            val delay = 12L // delay between each jump
            val velocity = 0.8 // velocity
            val radius = 2.0 // radius of particle ring in blocks
            val particleCount = 16
            val dur = 100 // in ticks, used to keep track of when to stop resetting fall distance

            object : BukkitRunnable() {
                var index = 0
                override fun run() {

                    // jump
                    p.velocity = Vector(p.velocity.x, velocity, p.velocity.z)

                    // sfx
                    p.world.playSound(p.location, Sound.ENTITY_BAT_TAKEOFF, 1f, 0.7f)

                    // particles
                    val points = MiscUtils().getPoints(p.x, p.y + 1.5, p.z, radius, particleCount, p.world)
                    points.forEach {
                        Particle.CLOUD.builder()
                            .location(it)
                            .offset(0.05, 0.0, 0.05)
                            .extra(0.02)
                            .spawn()
                    }

                    index++
                    if (index >= jumps) cancel()
                }
            }.runTaskTimer(plugin, 0L, delay)

            // cancel fall damage (too lazy to make a listener for this)
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    p.fallDistance = 0.0f
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

            if (KirbyListeners.Foo.playerSkins.contains(p)) {
                p.playerProfile.textures.skin = KirbyListeners.Foo.playerSkins[p] ?: return
                p.sendMessage(Component.text("Identity reset!").color(NamedTextColor.GREEN))
            } else {
                p.sendMessage(Component.text("You haven't stolen anyone's identity!").color(NamedTextColor.RED))
            }

            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 0

        p.addPotionEffect(PotionEffect(PotionEffectType.WEAVING, dur, amp))
    }

    override fun passiveB(p: Player) {
        TODO("Not yet implemented")
    }
}