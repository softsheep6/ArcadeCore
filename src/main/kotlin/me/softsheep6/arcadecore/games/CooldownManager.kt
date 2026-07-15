package me.softsheep6.arcadecore.games

import me.softsheep6.arcadecore.ArcadeCore
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

class CooldownManager(private val plugin: ArcadeCore) : Listener {

    /*
    methods:
        setAbilityCD(p, ability, cd)
        getAbilityCD(p, ability)
        isAbilityOnCD(p, ability)
     */


    // makes sure all players have cooldown pdcs
    val keyA = NamespacedKey(plugin, "ability-a")
    val keyB = NamespacedKey(plugin, "ability-b")
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val pdc = e.player.persistentDataContainer
        if (!pdc.has(keyA)) {
            pdc.set(keyA, PersistentDataType.INTEGER, 0)
        }
        if (!pdc.has(keyB)) {
            pdc.set(keyB, PersistentDataType.INTEGER, 0)
        }
        if (pdc.has(keyA) && pdc.has(keyB)) {
            pdc.set(keyA, PersistentDataType.INTEGER, 0)
            pdc.set(keyB, PersistentDataType.INTEGER, 0)
        }
    }

    fun setAbilityCD(p: Player, ab: Ability, cd: Int) {
        val pdc = p.persistentDataContainer
        if (!pdc.has(keyA) || !pdc.has(keyB)) return

        // sets a starting cooldown, then starts a bukkitrunnable to decrement cooldown until it reaches 0
        if (ab == Ability.ABILITY_A) {
            pdc.set(keyA, PersistentDataType.INTEGER, cd)
            object : BukkitRunnable() {
                override fun run() {
                    pdc.set(keyA, PersistentDataType.INTEGER, pdc.get(keyA, PersistentDataType.INTEGER)!! - 1)
                    if (pdc.get(keyA, PersistentDataType.INTEGER) == 0) cancel()
                }
            }.runTaskTimer(plugin, 20, 20)
        } else if (ab == Ability.ABILITY_B) {
            pdc.set(keyB, PersistentDataType.INTEGER, cd)
            object : BukkitRunnable() {
                override fun run() {
                    pdc.set(keyB, PersistentDataType.INTEGER, pdc.get(keyB, PersistentDataType.INTEGER)!! - 1)
                    if (pdc.get(keyB, PersistentDataType.INTEGER) == 0) cancel()
                }
            }.runTaskTimer(plugin, 20, 20)
        }
    }

    // do i even need this? probably not. but it just doesn't feel right to leave a set method without a get ... like peanut butter without the jelly, yk ??
    fun getAbilityCD(p: Player, ab: Ability): Int? {
        val pdc = p.persistentDataContainer
        if (!pdc.has(keyA) || !pdc.has(keyB)) return null

        if (ab == Ability.ABILITY_A) {
            return pdc.get(keyA, PersistentDataType.INTEGER)
        } else if (ab == Ability.ABILITY_B) {
            return pdc.get(keyB, PersistentDataType.INTEGER)
        }
        return null
    }

    fun isAbilityOnCD(p: Player, ab: Ability): Boolean {
        val pdc = p.persistentDataContainer
        if (!pdc.has(keyA) || !pdc.has(keyB)) return false

        if (ab == Ability.ABILITY_A) {
            return pdc.get(keyA, PersistentDataType.INTEGER) != 0
        } else if (ab == Ability.ABILITY_B) {
            return pdc.get(keyB, PersistentDataType.INTEGER) != 0
        }
        return true
    }
}