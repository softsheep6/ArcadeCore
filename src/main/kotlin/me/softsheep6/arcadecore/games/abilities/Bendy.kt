package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Bendy(private val plugin: ArcadeCore) : AbstractGame() {

    // cooldowns in seconds
    val abilityACD = 3
    val abilityBCD = 3

    override fun abilityA(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_A)) {
            p.sendMessage(Component.text("Ability A is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability A activated!").color(NamedTextColor.GREEN))


            // configurable values
            val radius = 5
            val height = 2
            val dur: Long = 100 // in ticks
            val particleSpeed = 0.1

            // black concrete cylinder
            val loc = p.location
            val blockLocs: ArrayList<Location> = ArrayList()
            val blockMaterials: ArrayList<Material> = ArrayList()
            for (i in -radius..radius) {
                for (j in -radius..radius) {
                    for (k in height downTo-height) {
                        val blockLoc = loc.clone().add(i.toDouble(), k.toDouble(), j.toDouble())
                        if (blockLoc.block.isSolid && blockLoc.distance(loc) <= radius) {
                            blockLocs.add(blockLoc)
                            blockMaterials.add(blockLoc.block.type)
                            // if the block is an inventory, drop its contents on the ground. i cant freaking figure out how to save a blockstate for later i cant freaking do it ahhhhhhhhhhhhhhhhh
                            if (blockLoc.block.state is BlockInventoryHolder) {
                                val inventoryHolder = blockLoc.block.state as BlockInventoryHolder
                                inventoryHolder.inventory.contents.forEach {
                                    if (it != null) blockLoc.world.dropItemNaturally(blockLoc, it) }
                            }
                            blockLoc.block.type = Material.BLACK_CONCRETE
                        }
                    }
                }
            }
            // sfx
            loc.world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 0f)


            // damage & blind nearby untrusted players. and particles too.
            object: BukkitRunnable() {
                var timer = dur
                override fun run() {
                    timer--

                    // apply negative effects to nearby players
                    val nearbyPlayers = loc.getNearbyPlayers(radius.toDouble(), height.toDouble())
                    nearbyPlayers.remove(p)
                    // TODO remove trusted players from the list
                    nearbyPlayers.forEach {
                        it.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 100, 2))
                        it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 100, 0))
                    }

                    // particles
                    Particle.EFFECT.builder()
                        .location(loc)
                        .count(10)
                        .offset(radius.toDouble()*.4, 0.0, radius.toDouble()*.4)
                        .color(10, 10, 10)
                        .extra(particleSpeed)
                        .spawn()


                    if (timer <= 0) {cancel()}
                }

            }.runTaskTimer(plugin, 0L, 1L)

            // replaces black concrete with what the blocks were before
            object : BukkitRunnable() {
                override fun run() {
                    for (i in blockMaterials.indices) {
                        blockLocs[i].block.type = blockMaterials[i]
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



            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA() {
        TODO("Not yet implemented")
    }

    override fun passiveB() {
        TODO("Not yet implemented")
    }
}