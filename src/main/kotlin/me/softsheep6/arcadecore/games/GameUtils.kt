package me.softsheep6.arcadecore.games

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.abilities.Bendy
import me.softsheep6.arcadecore.games.abilities.HollowKnight
import me.softsheep6.arcadecore.games.abilities.Mario
import me.softsheep6.arcadecore.games.abilities.Zelda
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import java.util.Locale.getDefault

class GameUtils(private val plugin: ArcadeCore) : Listener {

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
        // remove permanent passive stuff
        val game = getGame(p)
        when (game) {
            Game.NONE -> return
            Game.BENDY -> p.removePotionEffect(PotionEffectType.RESISTANCE)
            Game.HOLLOW_KNIGHT -> p.getAttribute(Attribute.SNEAKING_SPEED)?.baseValue = 0.3
            Game.MARIO -> p.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE)
            Game.ZELDA -> p.removePotionEffect(PotionEffectType.HASTE)
            // more
            else -> {}
        }

        p.persistentDataContainer.remove(key)
    }
    fun setGame(p: Player, g: Game) {
        clearGame(p)
        p.persistentDataContainer.set(key, PersistentDataType.STRING, g.name)

        // apply permanent passive stuff
        val game = g
        when (game) {
            Game.NONE -> clearGame(p)
            Game.BENDY -> Bendy(plugin).passiveA(p)
            Game.HOLLOW_KNIGHT -> HollowKnight(plugin).passiveA(p)
            Game.MARIO -> Mario(plugin).passiveA(p)
            Game.ZELDA -> Zelda(plugin).passiveA(p)
            // more
            else -> {}
        }
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