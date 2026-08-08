package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.listeners.DonkeyKongListeners.Foo.players
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f

class DonkeyKong(private val plugin: ArcadeCore) : AbstractGame() {
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
            val upVelocity = 1.0
            val downVelocity = -1.5
            val delay = 8 // in ticks, the delay before applying downward velocity
            // damage is configured in DonkeyKongListeners

            // up
            p.velocity = Vector(p.velocity.x, upVelocity, p.velocity.z)
            p.world.playSound(p.location, Sound.ENTITY_ARMADILLO_HURT, 1F, 0F)
            players.add(p)


            // down
            object : BukkitRunnable() {
                var ticks = 0
                override fun run() {
                    ticks++
                    // after delay ticks, apply velocity downwards
                    if (ticks == delay) p.velocity = Vector(p.velocity.x, downVelocity, p.velocity.z)
                    else if (ticks > delay * 4) { cancel(); players.remove(p) }
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
            val count = 3 // number of barrels thrown
            val dur = 200L // time before barrels disappear, in ticks
            val delay = 15L // delay between each barrel spawning, in ticks
            val speedMultiplier = 0.75 // barrel speed
            val damage = 2.0 // barrel damage
            val damageRadius = 0.75 // radius in blocks to check for nearby players around each barrel
            val blockParticleCount = 100
            val explosionParticleCount = 5

            val pigs = HashMap<Pig, Vector>() // don't ask why i'm using pigs. i could use any living entity and it'd work the exact same i just thought pigs would be funnier
            val displays = ArrayList<ItemDisplay>()

            // buffer spawns
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    p.world.spawn(p.location, Pig::class.java) { pig ->
                        pigs[pig] = p.location.clone().direction.setY(-0.2).normalize().multiply(speedMultiplier)

                        pig.velocity = pigs[pig] ?: return@spawn
                        pig.isSilent = true
                        pig.isInvisible = true
                        pig.isInvulnerable = true
                        pig.isCollidable = false

                        // model stuff
                        val loc = p.location.clone().add(0.0,-0.5,0.0)
                        loc.pitch = 0f
                        val itemDisplay = p.world.spawn(loc, ItemDisplay::class.java) {
                            val item = ItemStack.of(Material.IRON_SWORD)
                            val meta = item.itemMeta
                            val strings = ArrayList<String>()
                            val cmd = meta.customModelDataComponent
                            strings.add("barrel")
                            cmd.strings = strings
                            meta.setCustomModelDataComponent(cmd)
                            item.itemMeta = meta
                            it.setItemStack(item)

                            val transformation = Transformation(Vector3f(), AxisAngle4f((Math.toRadians(90.0)).toFloat(), 0.0f, 0.0f, 1.0f), Vector3f(1f,1f,1f), AxisAngle4f())
                            it.transformation = transformation

                            it.teleportDuration = 1
                            displays.add(it)
                        }
                        pig.addPassenger(itemDisplay)

                        // sfx
                        p.world.playSound(p.location, Sound.ITEM_CROSSBOW_SHOOT, 1f, 0f)
                    }
                    index++
                    if (index >= count) cancel()
                }
            }.runTaskTimer(plugin, 0L, delay)

            // repeatedly set velocity to ignore entity pushes, and then check for colliding player
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    pigs.forEach { pig ->
                        if (!pig.key.isValid) return@forEach
                        pig.key.velocity = pig.value

                        // check for nearby player(s)
                        val nearbyPlayers = p.world.getNearbyPlayers(pig.key.location, damageRadius)
                        nearbyPlayers.remove(p)
                        // remove trusted players here

                        // if there is at least one player nearby, damage them and kaboom the barrel
                        if (nearbyPlayers.isNotEmpty()) { // why is this even a method just do !x.isEmpty() this is so useless
                            // damage
                            nearbyPlayers.forEach { it.damage(damage) }

                            // remove barrel and display
                            if (pig.key.passengers.isNotEmpty()) pig.key.passengers.first().remove()
                            pig.key.remove()

                            // particles
                            Particle.BLOCK.builder()
                                .location(pig.key.location)
                                .data(Material.BARREL.createBlockData())
                                .count(blockParticleCount)
                                .offset(0.5, 0.5, 0.5)
                                .spawn()
                            Particle.EXPLOSION.builder()
                                .location(pig.key.location)
                                .count(explosionParticleCount)
                                .offset(0.5, 0.5, 0.5)
                                .spawn()

                            // sfx
                            p.world.playSound(pig.key.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f)
                            p.world.playSound(pig.key.location, Sound.ENTITY_ARROW_HIT, 1f, 0.6f)
                        }
                    }

                    displays.forEach {
                        if (it.isValid) {
                            val transformation = Transformation(Vector3f(),
                                AxisAngle4f((Math.toRadians(90.0)).toFloat(), 0.0f, 0.0f, 1.0f),
                                Vector3f(1f,1f,1f),
                                AxisAngle4f(Math.toRadians(index.toDouble()*-10).toFloat(), 0f, 1f, 0f))
                            it.transformation = transformation
                            it.interpolationDuration = 1
                        }
                    }

                    index++
                    if (index > dur) cancel()
                }
            }.runTaskTimer(plugin, 0L, 1L)

            // remove pigs after dur
            object : BukkitRunnable() {
                override fun run() {
                    pigs.forEach { it.key.remove() }
                    displays.forEach { it.remove() }
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
        // handled in DonkeyKongListeners
    }
}