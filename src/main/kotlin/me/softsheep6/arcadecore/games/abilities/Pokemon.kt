package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.MiscUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Guardian
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Pokemon (private val plugin: ArcadeCore) : AbstractGame() {
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
            val radius = 8.0
            val damage = 6.0 // damage of lightning bolts
            val delay = 10L // in ticks, lightning delay
            val slowDur = 120 // in ticks, duration of slowness effect
            val slowAmp = 2 // amplifier of slowness effect
            val particleCount = 100

            val nearbyPlayers = p.world.getNearbyPlayers(p.location, radius)
            nearbyPlayers.remove(p)
            // remove trusted players here
            if (nearbyPlayers.isEmpty()) return

            // sfx & particles
            p.world.playSound(p.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 2f)
            Particle.WAX_OFF.builder()
                .location(p.location.clone().add(0.0, 1.0, 0.0))
                .offset(2.0,1.0,2.0)
                .extra(2.0)
                .count(particleCount)
                .spawn()

            object : BukkitRunnable() {
                override fun run() {

                    nearbyPlayers.forEach {
                        // lightning
                        it.world.strikeLightningEffect(it.location)
                        it.world.strikeLightningEffect(it.location)
                        it.world.playSound(it.location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 0f)
                        p.world.playSound(p.location, Sound.ITEM_TRIDENT_THUNDER, 1f, 0f)

                        // damage
                        it.damage(damage)

                        // slowness
                        it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, slowDur, slowAmp))
                    }
                }
            }.runTaskLater(plugin, delay)




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
            val radius = 8.0
            val damage = 6.0 // beam damage
            val dur = 20L // duration of beam
            val particleCount = 64

            val player = MiscUtils().getNearestPlayer(p, radius)
            var points: List<Location>
            if (player == null) return

            // sfx
            p.world.playSound(p.location, Sound.ENTITY_NAUTILUS_DASH, 1f, 2f)
            p.world.playSound(p.location, Sound.ENTITY_DROWNED_SWIM, 1f, 0f)

            // beam
            val laser = p.world.spawn(p.location.clone().add(0.0,1.25,0.0), Guardian::class.java) {
                it.isInvisible = true
                it.isInvulnerable = true
                it.isCollidable = false
                it.isSilent = true
                it.setAI(false)
                it.setNoPhysics(true)
                it.target = player
                it.setLaser(true)
                it.laserTicks = -10
            }
            object : BukkitRunnable() {
                var index = 0
                override fun run() {

                    laser.teleport(p.location.clone().add(0.0,1.25,0.0))
                    laser.laserTicks = -10

                    index++
                    if (index >= dur) {
                        cancel()
                        laser.remove()
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L)

            // particles
            object : BukkitRunnable() {
                var index = 0
                override fun run() {

                    points = MiscUtils().getLinePoints(p.location.clone().add(0.0,1.25,0.0), player.location.clone().add(0.0,1.25,0.0), particleCount)
                    points.forEach {
                        Particle.BUBBLE_POP.builder()
                            .location(it)
                            .count(1)
                            .offset(0.1,0.1,0.1)
                            .extra(0.0)
                            .spawn()
                    }

                    index++
                    if (index >= dur) {
                        // damage the beam victim
                        player.damage(damage)
                        cancel()
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L)



            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 1

        p.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, dur, amp))
    }

    override fun passiveB(p: Player) {
        // configurable
        val chance = 18.7 // as a percentage
        val damage = 6.0

        val random = (Math.random() * 100)
        if (random < chance) {
            p.world.strikeLightningEffect(p.location)
            p.world.playSound(p.location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 0f)
            p.damage(damage)
        }
    }
}