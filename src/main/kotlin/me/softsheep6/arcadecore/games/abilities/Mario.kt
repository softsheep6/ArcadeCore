package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.FluidCollisionMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class Mario(private val plugin: ArcadeCore) : AbstractGame() {

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
            val rayDistance = 30.0 // in blocks, distance that raytrace checks for players
            val raySize = 1.0 // in blocks, width of the raytrace (i think?)
            val dur = 20L // in ticks, time until target's size is reverted to normal

            // raytrace to check for target that the user is aiming at
            val rayTrace = p.world.rayTrace(p.location, p.location.direction, rayDistance, FluidCollisionMode.NEVER, true, raySize, null)
            val hit = rayTrace?.hitEntity ?: return

            // get player (if one exists), scale them up, do particles, then dur ticks later revert their size.
            if (hit !is Player || hit == p) return
            hit.getAttribute(Attribute.SCALE)?.baseValue = 1.5
            object : BukkitRunnable() {
                override fun run() {
                    hit.getAttribute(Attribute.SCALE)?.baseValue = 1.0
                }
            }.runTaskLater(plugin, dur)



            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_A, abilityACD)
        }
    }

    override fun abilityB(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_A)) {
            p.sendMessage(Component.text("Ability A is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability A activated!").color(NamedTextColor.GREEN))




            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_A, abilityACD)
        }
    }

    override fun passiveA(p: Player) {
        TODO("Not yet implemented")
    }

    override fun passiveB(p: Player) {
        TODO("Not yet implemented")
    }
}