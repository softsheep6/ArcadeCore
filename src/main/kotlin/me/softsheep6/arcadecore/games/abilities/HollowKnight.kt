package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.OtherStuff
import me.softsheep6.arcadecore.games.listeners.HollowKnightListeners.Foo.player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class HollowKnight(private val plugin: ArcadeCore) : AbstractGame() {

    // cooldowns in seconds
    val abilityACD = 3
    val abilityBCD = 3

    // KNOWN BUGS //
    /*
    -- none atm!
     */
    // TODO:
    //  (maybe) a particle shockwave of sorts for ability A, use some black/gray particle (maybe
    //  enchanted hit particles? altho theyre more bluish)
    //  trusted player stuffs. as always.

    override fun abilityA(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_A)) {
            p.sendMessage(Component.text("Ability A is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability A activated!").color(NamedTextColor.GREEN))


            // configurable
            val upVelocity = 1.5
            val downVelocity = -2.0
            val delay = 15 // in ticks, the delay before applying downward velocity
            val radius = 4.0 // in blocks, radius of particles (damage radius is in HollowKnightListeners)
            val particleCount = 32
            // damage is configured in HollowKnightListeners

            // up
            p.velocity = Vector(p.velocity.x, upVelocity, p.velocity.y)
            p.world.playSound(p.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1F, 0.5F)
            player = p

            // particles
            val points = OtherStuff().getPoints(p.x, p.y + 1.5, p.z, radius, particleCount, p.world)
            points.forEach {
                Particle.CLOUD.builder()
                    .location(it)
                    .offset(0.1, 0.0, 0.1)
                    .extra(0.0)
                    .spawn()
            }

            // down
            object : BukkitRunnable() {
                var ticks = 0
                override fun run() {
                    ticks++
                    // after delay ticks, apply velocity downwards
                    if (ticks == delay) p.velocity = Vector(p.velocity.x, downVelocity, p.velocity.y)
                    else if (ticks > delay * 2) { cancel(); player = null }
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
            val horizontalVelMultiplier = 1.4
            val verticalVel = 0.4
            val dur = 25 // in ticks, how long nearby players should be checked for (to give them withering)
            val radius = .8 // in blocks, radius of getNearbyPlayers
            val effectDur = 160 // in ticks, duration of withering
            val effectAmp = 2 // strength of withering
            val particleRadius = .25
            val particleCount = 20

            // apply velocity
            val dir = p.location.direction
            p.velocity = Vector(dir.x * horizontalVelMultiplier, verticalVel, dir.z * horizontalVelMultiplier)

            // sfx
            p.world.playSound(p.location, Sound.ITEM_FIRECHARGE_USE, 1F, 1.5F)
            p.world.playSound(p.location, Sound.BLOCK_FIRE_AMBIENT, 1F, 2F)

            // check for nearby players & do particles
            object : BukkitRunnable() {
                var ticks = 0
                override fun run() {
                    ticks++
                    if (ticks > dur) return

                    // particles
                    Particle.INSTANT_EFFECT.builder()
                        .location(p.location.clone().add(0.0, 1.0, 0.0))
                        .offset(particleRadius, particleRadius*1.5, particleRadius)
                        .color(0,0,0)
                        .count(particleCount)
                        .spawn()

                    // apply wither to nearby untrusted players
                    val nearbyPlayers = p.world.getNearbyPlayers(p.location, radius)
                    nearbyPlayers.remove(p)
                    // remove trusted players
                    if (nearbyPlayers.isEmpty()) return
                    nearbyPlayers.forEach {
                        it.addPotionEffect(PotionEffect(PotionEffectType.WITHER, effectDur, effectAmp))
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L)

            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }


    override fun passiveA(p: Player) {
        // configurable
        val sneakSpeed = 0.75 // equivalent to swift sneak iii

        p.getAttribute(Attribute.SNEAKING_SPEED)?.baseValue = sneakSpeed
    }

    override fun passiveB(p: Player) {
        // configurable
        val dur = 20 // in ticks
        val amp = 0
        val chance = 27.8 // as a percentage

        val random = (Math.random() * 100)
        if (random < chance) p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, dur, amp))
    }

}