package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameUtils
import me.softsheep6.arcadecore.games.listeners.KirbyListeners.Foo.playerSkins
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.net.URL

class KirbyListeners(private val plugin: ArcadeCore) : Listener {

    object Foo {
        val playerSkins = HashMap<Player, URL>()
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        // return if the dead player was not killed by a player with kirby game
        if (e.damageSource.directEntity !is Player) return
        val p = e.damageSource.directEntity as Player
        val dead = e.player
        if (GameUtils(plugin).getGame(p) != Game.KIRBY) return

        // save their old skin
        playerSkins[p] = p.playerProfile.textures.skin ?: return

        // new skin
        setSkin(p, dead)
    }


    fun setSkin(p: Player, skinPlayer: Player) {
        println(p.playerProfile)
        val profile = p.playerProfile
        profile.textures.skin = skinPlayer.playerProfile.textures.skin ?: return
        p.playerProfile = profile
        println(p.playerProfile)
    }
}