package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.listeners.StreetFighterListeners
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class StreetFighter(private val plugin: ArcadeCore) : AbstractGame() {
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
            val chargeTime = 100 // in ticks, time taken to charge up the orb
            val deg = 5.0 // degrees of rotation per tick
            val scale = 3.0 // scale of the orb after finishing charge up
            val dist = 2.25 // distance of the orb from the player, in blocks
            val slowAmp = 1 // amplifier of slowness effect
            val particleCount = 5 // orb particle count
            val particleOffset = 0.25 // orb particle xyz offset

            // apply slowness
            p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, chargeTime, slowAmp))
            
            // spawn orb
            val display = p.world.spawn(Location(p.world, p.x, p.y+1, p.z), ItemDisplay::class.java) {
                it.teleportDuration = 1

                val item = ItemStack(Material.DIAMOND_SWORD)
                val meta = item.itemMeta
                val cmd = meta.customModelDataComponent
                val strings = ArrayList<String>()
                strings.add("orb")
                cmd.strings = strings
                meta.setCustomModelDataComponent(cmd)
                item.itemMeta = meta
                it.setItemStack(item)
            }

            // tp, spin, & grow orb, particles, sfx
            object : BukkitRunnable() {
                var index = 0
                var scaleIndex = 0
                override fun run() {

                    // teleport, rotate, scale
                    display.teleport(Location(p.world, p.x + dist*cos(Math.toRadians(p.yaw.toDouble()+90)), p.y+1, p.z + dist*sin(Math.toRadians(p.yaw.toDouble()+90))))
                    val transformation = Transformation(
                        Vector3f(),
                        AxisAngle4f((Math.toRadians(deg)*index).toFloat(), 0f, 1f, 0f),
                        Vector3f(((scaleIndex.toFloat()/chargeTime)*scale).toFloat(), ((scaleIndex.toFloat()/chargeTime)*scale).toFloat(), ((scaleIndex.toFloat()/chargeTime)*scale).toFloat()),
                        AxisAngle4f()
                    )
                    display.transformation = transformation

                    // particles
                    Particle.INSTANT_EFFECT.builder()
                        .location(display.location)
                        .count(particleCount/3)
                        .color(120, 180, 255)
                        .offset(particleOffset, particleOffset, particleOffset)
                        .spawn()
                    Particle.INSTANT_EFFECT.builder()
                        .location(display.location)
                        .count(particleCount/3)
                        .color(150, 175, 225)
                        .offset(particleOffset, particleOffset, particleOffset)
                        .spawn()
                    Particle.INSTANT_EFFECT.builder()
                        .location(display.location)
                        .count(particleCount/3)
                        .color(100, 149, 255)
                        .offset(particleOffset, particleOffset, particleOffset)
                        .spawn()

                    // sfx (stops playing when charged up)
                    if (index < chargeTime) p.world.playSound(display.location, Sound.ENTITY_WITHER_SHOOT, 1f, ((index.toDouble()/chargeTime)*1.5+0.5).toFloat())

                    index++
                    if (index < chargeTime) scaleIndex++ // only increase scale if not fully charged
                    else {
                        // when charged up, add display to hashmap and cancel task after it's fired (kept track of with glowing)
                        if (display.isGlowing) {cancel(); return}
                        StreetFighterListeners.Foo.chargedPlayers[p] = display
                    }
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
            val strengthAmp = 2 // amplifier of strength effect
            val resistanceAmp = 1 // amplifier of resistance effect
            val effectDur = 360 // duration of effects, in ticks
            val particleCount = 50
            val particleOffset = 0.5

            // add effects
            p.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, effectDur, strengthAmp))
            p.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, effectDur, resistanceAmp))

            // sfx
            p.world.playSound(p.location, Sound.ITEM_SPEAR_LUNGE_3, 1f, 0.5f)

            // particles
            Particle.SOUL_FIRE_FLAME.builder()
                .location(p.location.clone().add(0.0, 1.0, 0.0))
                .count(particleCount)
                .offset(particleOffset, particleOffset, particleOffset)
                .extra(0.0)
                .spawn()
            Particle.SOUL.builder()
                .location(p.location.clone().add(0.0, 1.0, 0.0))
                .count(particleCount)
                .offset(particleOffset, particleOffset, particleOffset)
                .extra(0.0)
                .spawn()

            // add player to set for cool slash particles on hit
            StreetFighterListeners.Foo.buffedPlayers.add(p)
            object : BukkitRunnable() {
                override fun run() { StreetFighterListeners.Foo.buffedPlayers.remove(p) }
            }.runTaskLater(plugin, effectDur.toLong())


            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 3

        p.addPotionEffect(PotionEffect(PotionEffectType.LUCK, dur, amp))
    }

    override fun passiveB(p: Player) {
        // configurable
        val dur = 160 // in ticks
        val amp = 2
        val chance = 16.4 // as a percentage

        val random = (Math.random() * 100)
        if (random < chance) p.addPotionEffect(PotionEffect(PotionEffectType.POISON, dur, amp))
    }
}