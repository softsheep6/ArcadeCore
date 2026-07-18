package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.OtherStuff
import me.softsheep6.arcadecore.games.listeners.BendyListeners.Foo.ignoredPlayers
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class BendyListeners(private val plugin: ArcadeCore) : Listener {

    // dont target ability user
    object Foo {
        val ignoredPlayers = ArrayList<Player>()
    }
    @EventHandler
    fun onEntityTarget(e: EntityTargetEvent) {
        if (e.entityType != EntityType.SKELETON || e.target?.type != EntityType.PLAYER) return
        if (ignoredPlayers.contains(e.target)) {
            // reassign target and cancel event
            val nearestPlayer = OtherStuff().getNearestPlayer(e.target as Player, 50.0)
            e.target = nearestPlayer
            (e.entity as Mob).target = nearestPlayer
            e.isCancelled = true
        }
    }

    // do stuff with arrow
    @EventHandler
    fun onEntityShootBow (e: EntityShootBowEvent) {
        // return if shooter is not a skeleton with a mending helmet
        if (e.entityType != EntityType.SKELETON) return
        if (!(e.entity as Skeleton).equipment.helmet.enchantments.contains(Enchantment.MENDING)) return

        // hide original arrow
        val arrow = e.projectile
//      arrow.setGravity(false)
        arrow.isVisibleByDefault = false
        arrow.isSilent = true

        // spawn display entity with custom model
        val arrowDisplay: ItemDisplay = e.entity.world.spawn(e.entity.location, ItemDisplay::class.java, false) {
            // item and cmd stuff
            val item = ItemStack.of(Material.WOODEN_SWORD)
            val meta = item.itemMeta
            val strings = ArrayList<String>()
            val cmd = meta.customModelDataComponent

            strings.add("bendy-projectile")
            cmd.strings = strings
            meta.setCustomModelDataComponent(cmd)
            item.itemMeta = meta
            it.setItemStack(item)

            it.teleportDuration = 1
        }

        // tp loop
        object : BukkitRunnable() {
            override fun run() {
                if (!arrow.isValid) { // exit if arrow no longer exists (picked up or despawned)
                    cancel()
                    arrowDisplay.remove()
                    return
                }
                arrowDisplay.teleport(arrow) // teleport display to arrow
            }
        }.runTaskTimer(plugin, 0L, 1L)

        // kill arrow after a while (idk how laggy display entities are but just in case)
        object : BukkitRunnable() {
            override fun run() {
                if (arrow.isValid) arrow.remove()
                if (arrowDisplay.isValid) arrowDisplay.remove()
            }
        }.runTaskLater(plugin, 600L)
    }




}