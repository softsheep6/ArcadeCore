package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.listeners.SubnauticaListeners.Foo.waterOrigins
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class Subnautica (private val plugin: ArcadeCore) : AbstractGame() {
// KNOWN BUGS //
    /*
    -- none atm!
     */
    // TODO:
    //  n/a
    //

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
            val radius = 5
            val duration = 360

            val blocks: ArrayList<Location> = ArrayList()
            waterOrigins.add(p.location)

            // set air blocks within radius distance to water
            for (i in -radius..radius) {
                for (j in -radius..radius) {
                    for (k in -radius..radius) {

                        val loc = Location(p.world, i + p.x, j + p.y, k + p.z)
                        if (loc.distance(p.location) > radius) continue

                        if (loc.block.type == Material.AIR) {
                            blocks.add(loc)
                            loc.block.type = Material.WATER
                        }
                    }
                }
            }

            // after duration ticks have passed, set water back to air (assuming it's still water)
            object : BukkitRunnable() {
                override fun run() {
                    for (loc in blocks) {
                        if (loc.block.type == Material.WATER) loc.block.type = Material.AIR
                    }
                    waterOrigins.remove(p.location)
                }
            }.runTaskLater(plugin, duration.toLong())




            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_A, abilityACD)
        }
    }

    override fun abilityB(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_B)) {
            p.sendMessage(Component.text("Ability B is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability B activated!").color(NamedTextColor.GREEN))



            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        TODO("Not yet implemented")
    }

    override fun passiveB(p: Player) {
        TODO("Not yet implemented")
    }
}