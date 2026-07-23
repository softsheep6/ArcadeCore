package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.MiscUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Zelda(private val plugin: ArcadeCore) : AbstractGame() {

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
            val radius = 10.0 // in blocks
            val noOfDividingPoints = 128
            val dur = 300L // in ticks
            val delay = 40L // in ticks
            val chargeParticleCount = 2 // every tick
            val abilityParticleCount = 5 // every tick
            val strikeFrequency = 30 // in ticks, time before lightning strikes a nearby player
            val effectAmp = 3 // amplifier of slowness effect inflicted on ability user
            val lightningDamage = 5.0

            val loc = p.location

            // bossbar for ability charging up
            val bar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SOLID)
            bar.addPlayer(p)
            bar.isVisible = true
            bar.progress = 0.0

            // slowness
            p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, delay.toInt(), effectAmp))

            // charging up ability (bar progress, particles, and sfx)
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    index++
                    if (index > delay) {
                        cancel()
                        bar.removeAll()
                        return
                    }

                    // prevent error from progress bar going over 1.0
                    val progress = index.toDouble()/delay
                    if (progress > 1.0) bar.progress = 1.0
                    else bar.progress = progress

                    // particles & sfx but not every tick cause that was too much
                    if (index % 4 == 0) {
                        Particle.ANGRY_VILLAGER.builder().location(p.location).offset(1.0, 1.0, 1.0).count(chargeParticleCount).spawn()
                        // pitch can be from 0-2, so to increase pitch uniformly, multiply index/delay by 2
                        p.world.playSound(p.location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, .6F, (index.toFloat() / delay) * 2)
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L)

            // the actual ability
            object : BukkitRunnable() {
                var index = 0
                val points = MiscUtils().getPoints(loc.x, loc.y, loc.z, radius, noOfDividingPoints, p.world)
                override fun run() {
                    index++
                    if (index > dur) cancel()

                    // particles (circle and ambient)
                    points.forEachIndexed { i, _ ->
                        for (j in 0..2) {
                            Particle.ENTITY_EFFECT.builder()
                                .location(points[i].clone().add(0.0, j.toDouble(), 0.0))
                                .extra(0.0)
                                .count(0)
                                .offset(0.0, 0.0, 0.0)
                                .color(255,255,0)
                                .spawn()
                        }
                    }
                    Particle.WAX_ON.builder().location(loc).offset(radius, radius, radius).count(abilityParticleCount).spawn()
                    Particle.WAX_OFF.builder().location(loc).offset(radius, radius, radius).count(abilityParticleCount).spawn()

                    // lightning (every strikeFrequency ticks)
                    if (((index-1) % strikeFrequency) == 0) {
                        val nearbyPlayers = loc.getNearbyPlayers(radius)
                        nearbyPlayers.remove(p)
                        // remove trusted players here
                        if (!nearbyPlayers.isEmpty()) {
                            val rand = (Math.random() * nearbyPlayers.size).toInt() // random number 0 to number of players
                            var randPlayer = nearbyPlayers.first()
                            // no idea why i can't just do nearbyPlayers.get(rand) probably some quirk of kotlin so here have a very unnecessary foreach loop
                            nearbyPlayers.forEachIndexed { i, player ->
                                if (i == rand) randPlayer = player
                            }
                            // strike lightning (just as an effect cause lightning damage is low anyways and i don't wanna deal with fire and items being destroyed)
                            p.world.strikeLightningEffect(randPlayer.location)
                            // & deal damage
                            randPlayer.damage(lightningDamage, p)
                            // oh wait sfx too
                            p.world.playSound(randPlayer.location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 0f)
                        }
                    }

                    // sfx once at the start
                    if (index == 1) p.world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 0f)
                }
            }.runTaskTimer(plugin, delay, 1L)


            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_A, abilityACD)
        }
    }

    override fun abilityB(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_B)) {
            p.sendMessage(Component.text("Ability B is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability B activated!").color(NamedTextColor.GREEN))

            // behold! the one line ability !
            p.health = p.getAttribute(Attribute.MAX_HEALTH)?.value ?: return

            // sadly we need flashy things too....can't just have the functionality can we ...
            p.world.playSound(p.location, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1f)
            Particle.HEART.builder()
                .location(p.location)
                .offset(1.0,1.0,1.0)
                .count(10)
                .spawn()

            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 1

        p.addPotionEffect(PotionEffect(PotionEffectType.HASTE, dur, amp))
    }

    override fun passiveB(p: Player) {
        // everything handled in ZeldaListeners
    }
}