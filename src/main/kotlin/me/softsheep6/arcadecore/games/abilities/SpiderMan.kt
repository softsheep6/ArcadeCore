package me.softsheep6.arcadecore.games.abilities

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Ability
import me.softsheep6.arcadecore.games.AbstractGame
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.listeners.SpiderManListeners.Foo
import me.softsheep6.arcadecore.games.listeners.SpiderManListeners.Foo.blocks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class SpiderMan(private val plugin: ArcadeCore) : AbstractGame() {

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
            val webSpeed = .8 // i dont know what the unit is This is a certified #magicnumber
            val flingSpeed = 4 // x2
            val dur = 30L // in ticks

            var direction = Vector()
            val web = p.world.spawn(p.location.clone().add(0.0,1.0,0.0), Item::class.java) {
                direction = p.location.direction
                it.itemStack = ItemStack(Material.COBWEB)
                it.setGravity(false)
                it.thrower = p.uniqueId
                it.setCanPlayerPickup(false)
                it.isInvulnerable = true
                it.isGlowing = true
                it.velocity = direction.multiply(webSpeed)
            }
            p.world.playSound(p.location, Sound.ENTITY_FISHING_BOBBER_THROW, 1f, 0.8f)

            p.pose = Pose.USING_TONGUE // this is freaky as freak bro what mob even has a USING_TONGUE pose 😹😹😹

            // apply velocity, sfx, kill web
            object : BukkitRunnable() {
                override fun run() {
                    p.teleport(p.location.clone().add(0.0,0.5,0.0))
                    p.velocity = direction.clone().add(Vector(0.0,0.1,0.0)).multiply(flingSpeed)
                    p.world.playSound(p.location, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 0.5f)
                    web.remove()
                    p.flySpeed = 0.1001f // this is a stupid way to keep track of things whatever
                }
            }.runTaskLater(plugin, dur)

            // particles
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    index++
                    if (index > dur) { cancel() }
                    Particle.CLOUD.builder()
                        .location(web.location)
                        .offset(0.0,0.0,0.0)
                        .count(1)
                        .extra(0.0)
                        .spawn()
                }
            }.runTaskTimer(plugin, 0L, 1L)

            object : BukkitRunnable() {
                override fun run() {
                    // stupid stupid stupid
                    p.flySpeed = 0.1f
                }
            }.runTaskLater(plugin, 160L)

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
            val radius = 3
            val delay = 15L // time in ticks for the web cage to fully form
            val decay = 0.9 // decimal percent of cobwebs that will be missing. 0 = no cobwebs, 100 = all cobwebs
            val dur = 300L // time in ticks for web cage to expire
            val damage = 4.0 // damage done every half second

            val loc = p.location

            // get blocks
            var blockCount = 0
            for (i in -radius..radius) {
                for (j in -radius..radius) {
                    for (k in -radius..radius) {
                        blocks.add(loc.clone().add(i.toDouble(), j.toDouble(), k.toDouble()).block)
                        blockCount++
                    }
                }
            }

            // meow meow
            object : BukkitRunnable() {
                var index = 0
                override fun run() {
                    index++
                    if (index >= 3) cancel()

                    // iterate through nearby blocks
                    blocks.forEach {
                        when (index) {
                            // if the distance from the block to the ability location is within the stage's radius,
                            //   AND the block is an air block,
                            //   AND the random value is greater than the decay value,
                            //   then make the block a cobweb.
                            1 -> if (it.location.distance(loc) <= radius/3.0 && it.type == Material.AIR && Math.random() < decay) it.type = Material.COBWEB
                            2 -> if (it.location.distance(loc) <= radius*(2.0/3) && it.type == Material.AIR && Math.random() < decay) it.type = Material.COBWEB
                            3 -> if (it.location.distance(loc) <= radius.toDouble() && it.type == Material.AIR && Math.random() < decay) it.type = Material.COBWEB
                        }
                    }

                    // sfx
                    p.world.playSound(loc, Sound.BLOCK_COBWEB_FALL, 1f, (0.5 + (index*0.1)).toFloat())
                    p.world.playSound(loc, Sound.BLOCK_COBWEB_BREAK, 1f, (0.5 + (index*0.1)).toFloat())

                }
            }.runTaskTimer(plugin, 0L, delay/3)

            // damage nearby players
            object : BukkitRunnable(){
                var index = 0
                override fun run() {

                    val players = ArrayList<Player>()
                    p.world.getNearbyPlayers(loc, radius.toDouble()).forEach { if (it != p) players.add(it) } // add nearby player if player isn't the ability user

                    if (index % 10 == 0)
                    players.forEach {
                        it.damage(damage, p)
                    }

                    index++
                    if (index > dur) cancel()
                }
            }.runTaskTimer(plugin, delay, 1L)

            // remove web cage
            object : BukkitRunnable() {
                override fun run() {
                    for (i in 0..<blockCount) {
                        if (blocks[0].type == Material.COBWEB) blocks[0].type = Material.AIR
                        blocks.removeAt(0)
                    }
                }
            }.runTaskLater(plugin, dur)


            CooldownManager(plugin).setAbilityCD(p, Ability.ABILITY_B, abilityBCD)
        }
    }

    override fun passiveA(p: Player) {
        p.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER)?.baseValue = 0.0
    }

    override fun passiveB(p: Player) {
        // this method is called when player starts sneaking.
        // every tick, do the following:
        //  cancel and return if player is no longer sneaking
        //  check if player can climb (don't ask what this means i stole the code)
        //      if they are, apply velocity
        object : BukkitRunnable() {
            override fun run() {
                if (!p.isSneaking) {cancel(); return}
                if (Foo.checkClimbing(p)) {
                    val vec = (Vector(0.0, 0.35, 0.0))
                    p.velocity = vec
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}