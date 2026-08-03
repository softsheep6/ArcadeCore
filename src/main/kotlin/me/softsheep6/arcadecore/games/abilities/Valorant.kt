package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.listeners.ValorantListeners
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f
import kotlin.math.pow

class Valorant(private val plugin: ArcadeCore) : AbstractGame() {

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
            val horizontalVelMultiplier = 1.4
            val verticalVel = 0.4

            // apply velocity
            val dir = p.location.direction
            p.velocity = Vector(dir.x * horizontalVelMultiplier, verticalVel, dir.z * horizontalVelMultiplier)

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
            val swordCount = 5
            val dur = 80L // in ticks, time before swords expire by default. they will most likely be used up before this happens though
            val yOffset = 3 // in blocks, y offset of swords above the player
            val scale = 0.5f // scale of item displays

            val displays = ArrayList<ItemDisplay>()
            ValorantListeners.Foo.activeAbilities[p] = swordCount
            for (i in -swordCount..<swordCount step 2) {
                p.world.spawn(Location(p.world, p.x, p.y, p.z).clone().add((i+1)/4.0,yOffset - ((i + 1.0).pow(2.0) * 0.02),0.0), ItemDisplay::class.java) {
                    displays.add(it)

                    // item and cmd stuff
                    val item = ItemStack.of(Material.IRON_SWORD)
                    //val meta = item.itemMeta
                    //val strings = ArrayList<String>()
                    //val cmd = meta.customModelDataComponent
                    //strings.add("valorant")
                    //cmd.strings = strings
                    //meta.setCustomModelDataComponent(cmd)
                    //item.itemMeta = meta
                    it.setItemStack(item)

                    // transformation
                    val rotation = AxisAngle4f((Math.toRadians(135.0 - (i+1)*5)).toFloat(), 0f, 0f, 1f) // -45deg rotation to have the swords positioned vertically, instead of their usual tilt.
                    it.transformation = Transformation(Vector3f(), rotation, Vector3f(scale, scale, scale), AxisAngle4f())

                    // other
                    it.teleportDuration = 1
                }
            }

            // teleport displays to player. also kill displays after duration
            object : BukkitRunnable() {
                var index = 0
                override fun run() {

                    // j is for the arraylist (0-4), i is for the offset (-5,-3,-1,1,3)
                    for ((j, i) in (-swordCount..<swordCount step 2).withIndex()) {

                        // teleport
                        displays[j].teleport(Location(p.world, p.x, p.y, p.z).clone().add((i+1)/4.0,yOffset - ((i + 1.0).pow(2.0) * 0.02),0.0))

                        // i really REALLY wanted to have it follow the players rotation but no its just impossible to figure out im sorry im tooooo stupid...........
                        val rotation = AxisAngle4f((Math.toRadians(135.0 - (i+1)*5)).toFloat(), 0f, 0f, 1f)
                        displays[j].transformation = Transformation(Vector3f(), rotation, Vector3f(scale, scale, scale), AxisAngle4f())
                    }

                    index++
                    if (index > dur) {
                        cancel()
                        displays.forEach {
                            it.remove()
                            ValorantListeners.Foo.activeAbilities.remove(p)
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L)



            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 0

        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, dur, amp))
    }

    override fun passiveB(p: Player) {
        // configurable
        val dur = 60 // in ticks
        val amp = 2
        val chance = 17.5 // as a percentage

        val random = (Math.random() * 100)
        if (random < chance) p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, dur, amp))
    }
}