package me.softsheep6.arcadecore.games

import me.softsheep6.arcadecore.ArcadeCore
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import java.util.Locale.getDefault

class GameManager(private val plugin: ArcadeCore) : Listener {

    /*
    methods:
        hasGame(p)
        getGame(p)
        setGame(p, game)
        clearGame(p)
        ...
     */

    // makes sure all players have a game pdc
    val key = NamespacedKey(plugin, "game")
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val pdc = e.player.persistentDataContainer
        if (!pdc.has(key)) {
            pdc.set(key, PersistentDataType.STRING, "none")
            plugin.logger.info("Player ${e.player.name} joined the game for the first time!")
        }
    }



    fun clearGame(p: Player) {
        p.persistentDataContainer.remove(key)
    }
    fun setGame(p: Player, g: Game) {
        p.persistentDataContainer.set(key, PersistentDataType.STRING, g.name)
    }
    fun getGame(p: Player): Game {
        if (hasGame(p))
            return Game.valueOf(p.persistentDataContainer.get(key, PersistentDataType.STRING)!!.uppercase(getDefault()))
        return Game.NONE
    }
    fun hasGame(p: Player): Boolean {
        return p.persistentDataContainer.has(key, PersistentDataType.STRING)
    }

}