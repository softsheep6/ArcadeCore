package me.softsheep6.arcadecore.games

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.StunManager.Foo.stunnedPlayers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable

class StunManager(private val plugin: ArcadeCore) : Listener {

    object Foo {
        val stunnedPlayers = HashSet<Player>()
    }

    // stun player p for dur ticks, and show them a custom title
    fun stunPlayer(p: Player, dur: Long, title: Title) {
        stunnedPlayers.add(p)
        p.showTitle(title)
        object : BukkitRunnable() {
            override fun run() { stunnedPlayers.remove(p) }
        }.runTaskLater(plugin, dur)
    }
    // stun player p for dur ticks, and show them a default "STUNNED" title
    // LOOK AT ME IM OVERLOADING OMG OMG OMG IM SO OBJECT ORIENTED
    fun stunPlayer(p: Player, dur: Long) {
        stunnedPlayers.add(p)
        p.showTitle(Title.title(Component.text("STUNNED").style(Style.style(TextDecoration.BOLD)).color(NamedTextColor.YELLOW), Component.text(""), 0, dur.toInt(), 0))
        object : BukkitRunnable() {
            override fun run() { stunnedPlayers.remove(p) }
        }.runTaskLater(plugin, dur)
    }

    // actually stun the player
    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (stunnedPlayers.contains(e.player)) e.isCancelled = true
    }

}