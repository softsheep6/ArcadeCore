package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.OtherStuff
import me.softsheep6.arcadecore.games.listeners.BendyListeners.Foo.ignoredPlayers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.entity.Skeleton
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Bendy(private val plugin: ArcadeCore) : AbstractGame() {

    // cooldowns in seconds
    val abilityACD = 3
    val abilityBCD = 3

    // KNOWN BUGS //
    /*
    ABILITY 1
    -- overlapping ink circles can leave blocks behind after ability ends
    -- ink circle blocks can be broken (prevent with listeners for blockbreak, blockexplode(?), whatever the event for piston is)
    ABILITY 2
    -- minion arrows can hit ability user
     */
    // TODO:
    //  the models. whenever ben remakes them in JSON format.
    //  give the minion bows a configurable power enchant so damage can be modified
    //  also trusted player stuffs

    override fun abilityA(p: Player) {
        if (CooldownManager(plugin).isAbilityOnCD(p, Ability.ABILITY_A)) {
            p.sendMessage(Component.text("Ability A is on cooldown!").color(NamedTextColor.RED))
            return
        } else {
            p.sendMessage(Component.text("Ability A activated!").color(NamedTextColor.GREEN))


            // configurable values
            val radius = 5
            val height = 2
            val dur: Long = 300 // in ticks
            val particleSpeed = 0.1
            val blockTypes = ArrayList<Material>(listOf(Material.BLACK_CONCRETE, Material.COAL_BLOCK, Material.BLACK_WOOL))

            // black cylinder
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
                            val randBlock = (Math.random() * blockTypes.size).toInt()
                            blockLoc.block.type = blockTypes[randBlock]
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
                    // remove trusted players from the list here
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



            // replaces black concrete with what the blocks were before, after dur ticks have passed
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
            ignoredPlayers.add(p)
            // add trusted players to the arraylist too

            // configurable values
            val dur = 300L // in ticks
            val minions = ArrayList<Mob>()
            val minionDisplays = ArrayList<ItemDisplay>()
            val minionCount = 4
            val minionRadius = 4 // how many blocks from the player the minions are

            // spawn minions and display entities
            for (i in 0..<minionCount) {
                p.world.spawn(p.location, Skeleton::class.java, false) {
                    minions.add(it)
                    val equipment = it.equipment
                    equipment.setItemInMainHand(ItemStack(Material.BOW)) // configurable power level so damage can be modified here
                    val helmet = ItemStack.of(Material.LEATHER_HELMET)
                    helmet.addEnchantment(Enchantment.MENDING, 1) // adds a nonnatural enchant to know if a skeleton is a minion or a naturally spawned skeleton
                    equipment.helmet = helmet
                    it.isInvulnerable = true
                    it.isSilent = true
                    it.isCollidable = false
                    it.isVisibleByDefault = false
                }
                p.world.spawn(p.location, ItemDisplay::class.java, false) {
                    minionDisplays.add(it)
                    val item = ItemStack.of(Material.WOODEN_SWORD)
                    val cmd = item.itemMeta.customModelDataComponent
                    val strings = ArrayList<String>()
                    strings.add("bendy-minion")
                    cmd.strings = strings
                    it.setItemStack(item)

                }
            }


            // teleport minions/display entities around player
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    val locations = OtherStuff().getPoints(p.location.x, p.location.y, p.location.z, minionRadius, minionCount, p.world)
                    minions.forEachIndexed { i, it ->
                        if (!it.isValid) {
                            cancel()
                            return
                        }
                        it.teleport(Location(p.world, locations[i].x, locations[i].y, locations[i].z))
                    }
                    minionDisplays.forEachIndexed { i, it ->
                        if (!it.isValid) {
                            cancel()
                            return
                        }
                        it.teleport(Location(p.world, locations[i].x, locations[i].y, locations[i].z))
                    }
                    index++
                    if (index >= dur) cancel()
                }
            }.runTaskTimer(plugin, 0L, 1L)

            // sfx
            p.world.playSound(p.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 0f)


            // end ability
            object : BukkitRunnable() {
                override fun run() {
                    minions.forEach {
                        it.remove()
                        ignoredPlayers.remove(p)
                    }
                    minionDisplays.forEach { it.remove() }
                }
            }.runTaskLater(plugin, dur)


            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }


    override fun passiveA(p: Player) {
        // configurable
        val dur = -1 // in ticks
        val amp = 0

        p.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, dur, amp))
    }

    override fun passiveB(p: Player) {
        // configurable
        val dur = 100 // in ticks
        val amp = 2
        val chance = 16.9 // as a percentage

        val random = (Math.random() * 100)
        if (random < chance) p.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, dur, amp))
    }
}