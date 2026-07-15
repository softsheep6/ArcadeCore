package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.games.listeners.BendyListeners.Foo.ignoredPlayers
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Skeleton
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.EntityTargetEvent

class BendyListeners : Listener {

    object Foo {
        val ignoredPlayers = ArrayList<Player>()
    }
    @EventHandler
    fun onEntityTarget(e: EntityTargetEvent) {
        if (e.entityType != EntityType.SKELETON || e.target?.type != EntityType.PLAYER) return
        if (ignoredPlayers.contains(e.target)) {
            // reassign target and cancel event
            val players = e.entity.world.getNearbyPlayers(e.entity.location, 20.0)
            players.remove(e.target)
            if (!players.isEmpty()) e.target = players.first()
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityShootBow (e: EntityShootBowEvent) {
        // return if shooter is not a skeleton with a mending helmet
        if (e.entityType != EntityType.SKELETON) return
        if (!(e.entity as Skeleton).equipment.helmet.enchantments.contains(Enchantment.MENDING)) return

        val arrow = e.projectile
        arrow.setGravity(false)
        arrow.isInvisible = true
        arrow.isSilent = true

        // create display entity with custommodeldata, tasktimer that TPs display to arrow every tick, and dont forget to cancel task once arrow no longer exists
    }


}