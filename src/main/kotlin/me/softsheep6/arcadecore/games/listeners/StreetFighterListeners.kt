package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.StunManager
import me.softsheep6.arcadecore.games.listeners.StreetFighterListeners.Foo.chargedPlayers
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.damage.DamageType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class StreetFighterListeners(private val plugin: ArcadeCore) : Listener {

    object Foo {
        val chargedPlayers = HashMap<Player, ItemDisplay>()
        val buffedPlayers = HashSet<Player>()
    }

    // configurable
    val lifetime = 1200 // lifetime of fired orb, in ticks
    val speed = 0.1 // speed of orb, in blocks per tick (?)
    val radius = 0.75 // radius of nearby player check, in blocks
    val damage = 4.0 // damage of orb explosion
    val stunDur = 40L // duration of stun, in ticks
    val deg = 5.0 // degrees of rotation per tick
    val particleCount = 5 // orb particle count
    val particleOffset = 0.25 // orb particle xyz offset

    // fire orb if player with a charged orb left clicks
    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val p = e.player
        if (chargedPlayers.contains(p) && (e.action == Action.LEFT_CLICK_AIR || e.action == Action.LEFT_CLICK_BLOCK)) {

            val yaw = p.yaw
            val display = chargedPlayers[p]
            val displayY = display?.y ?: return

            chargedPlayers.remove(p)

            display.isGlowing = true
            display.glowColorOverride = Color.fromRGB(120, 180, 255)
            p.world.playSound(display.location, Sound.ENTITY_GHAST_SHOOT, 1f, 0.5f)
            object : BukkitRunnable() {
                var index = 0
                override fun run() {

                    // teleport orb forward
                    display.teleport(Location(p.world, display.x + speed*(index*cos(Math.toRadians(yaw.toDouble()+90))), displayY, display.z + speed*(index*sin(Math.toRadians(yaw.toDouble()+90)))))

                    // rotation
                    val transformation = Transformation(
                        Vector3f(),
                        AxisAngle4f((Math.toRadians(deg)*index).toFloat(), 0f, 1f, 0f),
                        Vector3f(display.transformation.scale),
                        AxisAngle4f()
                    )
                    display.transformation = transformation

                    // particles
                    Particle.INSTANT_EFFECT.builder()
                        .location(display.location)
                        .count(particleCount)
                        .color(120, 180, 255)
                        .offset(particleOffset, particleOffset, particleOffset)
                        .spawn()

                    // check for player collision (reused from donkey kong)
                    val nearbyPlayers = p.world.getNearbyPlayers(display.location, radius)
                    nearbyPlayers.remove(p)
                    // remove trusted players here

                    // if there is at least one player nearby, damage & stun them and kaboom the orb
                    if (nearbyPlayers.isNotEmpty()) {
                        // damage & stun
                        nearbyPlayers.forEach {
                            it.damage(damage)
                            StunManager(plugin).stunPlayer(it, stunDur)
                        }

                        // remove display
                        display.remove()
                        cancel()

                        // particles
                        Particle.EXPLOSION_EMITTER.builder()
                            .location(display.location)
                            .count(1)
                            .offset(0.0,0.0,0.0)
                            .spawn()
                        Particle.FLASH.builder()
                            .location(display.location)
                            .count(1)
                            .offset(0.0,0.0,0.0)
                            .color(Color.fromRGB(120, 180, 255))
                            .spawn()

                        // sfx
                        p.world.playSound(display.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.7f)
                    }

                    index++
                    if (index >= lifetime) {
                        cancel()
                        display.remove()
                    }
                }
            }.runTaskTimer(plugin, 0, 1)
        }
    }

    // slash particles on hit when 2nd ability is active
    @EventHandler
    fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
        if (e.entity !is Player || e.damager !is Player || e.damageSource.damageType != DamageType.PLAYER_ATTACK) return
        if (!Foo.buffedPlayers.contains(e.damager)) return

        // configurable


        val damager = e.damager as Player
        val damaged = e.entity as Player
        var yaw: Float
        var y: Double
        var loc: Location
        val distance = damager.location.distance(damaged.location)*0.25
        val particleCount = 7

        // the slash
        for (i in 0..<particleCount) {
            yaw = (damager.yaw - 45 + (i/particleCount.toDouble())*90).toFloat()
            y = damaged.y + 0.5 + (i/particleCount.toDouble())
            loc = Location(damaged.world, damaged.x + distance*cos(Math.toRadians(yaw.toDouble()-90)), y, damaged.z + distance*sin(Math.toRadians(yaw.toDouble()-90)))

            Particle.ENCHANTED_HIT.builder()
                .location(loc)
                .count(2)
                .offset(0.01,0.0,0.01)
                .extra(0.0)
                .spawn()
        }

    }
}