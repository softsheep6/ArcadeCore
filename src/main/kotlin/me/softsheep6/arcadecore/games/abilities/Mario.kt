package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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
            val dur = 300L // in ticks, time until target's size is reverted to normal
            val scale = 1.5 // value of modified scale attribute
            val volume = 0.25f // volume of sfx

            // raytrace to check for target that the user is aiming at
            val ray = p.world.rayTraceEntities(
                p.eyeLocation.add(p.location.getDirection()),
                p.eyeLocation.getDirection(),
                rayDistance
            ) { entity -> entity.uniqueId != p.uniqueId }
            // validate entity
            if (ray == null) return
            val hit = ray.hitEntity ?: return
            if (hit !is Player) return

            // scale player up & sfx
            hit.getAttribute(Attribute.SCALE)?.baseValue = scale
            hit.world.playSound(hit.location, "entity.player.mario", volume, 1f) // CUSTOM SFX OMG OMG OM G

            // particles
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    index++
                    if (index > dur) cancel()
                    Particle.COMPOSTER.builder()
                        .location(hit.location)
                        .offset(1.0, 2.0, 1.0)
                        .count(5)
                        .extra(0.2)
                        .spawn()
                }
            }.runTaskTimer(plugin, 0L, 1L)

            // reset scale after dur
            object : BukkitRunnable() {
                override fun run() {
                    hit.getAttribute(Attribute.SCALE)?.baseValue = 1.0
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

            // configurable
            val durability = 75 // durability of armor healed

            // get armor pieces in player's inventory
            val armorPieces = ArrayList<ItemStack>()
            p.inventory.forEach {
                if (it != null) {
                    if (Tag.ITEMS_ENCHANTABLE_ARMOR.isTagged(it.type)) armorPieces.add(it)
                }
            }

            // repair them !
            var piecesHealed = 0
            armorPieces.forEach {
                val meta = it.itemMeta
                (meta as Damageable).setMaxDamage(it.type.maxDurability.toInt())
                if (meta.hasMaxDamage()) {
                    val maxDamage = meta.maxDamage
                    val currentDamage = meta.damage
                    val currentDur = maxDamage - currentDamage

                    if (currentDur + durability > maxDamage) meta.damage = 0
                    else meta.damage = currentDamage - durability
                    it.itemMeta = meta
                    piecesHealed++
                }
            }

            // send message & sfx & some particles too
            p.sendMessage(Component.text(
                "$piecesHealed", NamedTextColor.GREEN)
                .append(Component.text(" pieces of armor healed for ", NamedTextColor.WHITE))
                .append(Component.text("$durability", NamedTextColor.GREEN))
                .append(Component.text(" durability each!", NamedTextColor.WHITE)))
            p.world.playSound(p.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            Particle.HAPPY_VILLAGER.builder()
                .location(p.location)
                .offset(1.0, 2.0, 1.0)
                .count(20)
                .extra(0.2)
                .spawn()




            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 4

        p.addPotionEffect(PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, dur, amp))
    }

    override fun passiveB(p: Player) {
        p.world.dropItemNaturally(p.location, ItemStack(Material.APPLE))
    }
}