package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.StunManager
import me.softsheep6.arcadecore.games.listeners.CastleCrashersListeners
import me.softsheep6.arcadecore.games.listeners.CastleCrashersListeners.Foo.BREAKS
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class CastleCrashers(private val plugin: ArcadeCore) : AbstractGame() {

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
            val radius = 5.0 // radius of players to freeze, in blocks
            val dur = 60L // duration of freeze, in ticks
            val slowDur = 120 // duration of slowness debuff, in ticks
            val slowAmp = 2 // amplifier of slowness debuff
            val iceBlockCount = 2 // number of ice blocks that will spawn at each frozen player
            val iceXZScale = 1.2f // x and z scales of ice blocks
            val iceYScale = 2f // y scale of ice blocks
            val particleCount = 100 // amount of particles that will spawn at each frozen player

            // freeze players
            val nearbyPlayers = p.world.getNearbyPlayers(p.location, radius)
            nearbyPlayers.remove(p)
            // remove trusted players here
            if (nearbyPlayers.isEmpty()) return // return if there's no one nearby
            nearbyPlayers.forEach { StunManager(plugin).stunPlayer(it, dur, Title.title(Component.text("FROZEN").style(Style.style(TextDecoration.BOLD)).color(NamedTextColor.AQUA), Component.text(""), 0, dur.toInt(), 0)) }

            // ice block display, sfx, and particles
            val iceBlocks = ArrayList<BlockDisplay>()
            nearbyPlayers.forEach { player ->
                // spawn displays
                for (i in 0..<iceBlockCount) {
                    player.world.spawn(Location(player.world, player.x, player.y, player.z), BlockDisplay::class.java) {
                        it.block = Material.ICE.createBlockData()
                        val transformation = Transformation(
                            Vector3f(-0.5f, (Math.random()*0.25).toFloat(), -0.5f),
                            AxisAngle4f((Math.random()).toFloat(), 0f, 1f, 0f),
                            Vector3f(iceXZScale, iceYScale, iceXZScale),
                            AxisAngle4f(-(Math.random()).toFloat(), 0f, 1f, 0f)
                        )
                        it.transformation = transformation
                        iceBlocks.add(it)
                    }
                }

                // sfx
                player.playSound(player.location, Sound.BLOCK_GLASS_BREAK, 1f, 0.5f)

                // particles
                Particle.ITEM_SNOWBALL.builder()
                    .location(player.location)
                    .count(particleCount)
                    .offset(1.0, 1.0, 1.0)
                    .spawn()
                Particle.DUST.builder()
                    .location(player.location)
                    .count(particleCount)
                    .offset(1.0, 1.0, 1.0)
                    .color(Color.SILVER, 1.5f)
                    .spawn()
            }
            p.world.playSound(p.location, Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f)


            // unfreeze players
            object : BukkitRunnable() {
                override fun run() {

                    // kill ice blocks
                    iceBlocks.forEach { it.remove() }

                    // slowness effect
                    nearbyPlayers.forEach {
                        it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, slowDur, slowAmp))
                    }
                }
            }.runTaskLater(plugin, dur)

            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_A, abilityACD)
        }
    }

    override fun abilityB(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_B)) {
            p.sendMessage(Component.text("Ability B is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability B activated!").color(NamedTextColor.GREEN))

            // waiting for tower to be built ...

            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 0

        p.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, dur, amp))
    }

    override fun passiveB(p: Player) {
        // number of breaks can be configured in CastleCrashersListeners

        CastleCrashersListeners.Foo.shieldBreaks[p] = BREAKS
    }
}