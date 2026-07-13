package me.softsheep6.arcadecore.commands

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.Locale.getDefault

class GameCommand(private val plugin: ArcadeCore) : TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        // command must be sent by a player and have 2 or 3 arguments
        if (sender !is Player) {
            return true
        }
        val p = sender
        if (!(args.size == 2 || args.size == 3)) {
            p.sendMessage(Component.text("Usage: /game <set|get|clear> <player> [game]").color(NamedTextColor.RED))
            return true
        }

        // make sure target is valid
        var targetFound = false
        var target: Player = p
        for (player in Bukkit.getOnlinePlayers()) {
            if (args[1].equals(player.name, true)) {
                target = player
                targetFound = true
            }
        }
        if (!targetFound) {
            p.sendMessage(Component.text("Player not found").color(NamedTextColor.RED))
            return true
        }



        // /game get
        if (args[0].equals("get", ignoreCase = true)) {
            p.sendMessage(Component.text("Player ${target.name} currently has ${GameManager(plugin).getGame(target)} equipped", NamedTextColor.GREEN))
            return true
        }


        // /game set
        if (args[0].equals("set", ignoreCase = true)) {
            // match game argument given with game enum
            val gameString = args[2]
            var game = Game.entries.last()
            var gameFound = false
            for (g in Game.entries) {
                if (gameString.equals(g.name, true)) {
                    game = g
                    gameFound = true
                }
            }
            if (!gameFound) {
                p.sendMessage(Component.text("Invalid game").color(NamedTextColor.RED))
                return true
            }

            // set game !
            GameManager(plugin).setGame(target, game)
            p.sendMessage(Component.text("Successfully set ${target.name} to $game", NamedTextColor.GREEN))
            return true
        }


        // /game clear
        if (args[0].equals("clear", ignoreCase = true)) {
            GameManager(plugin).clearGame(target)
            p.sendMessage(Component.text("Successfully cleared ${target.name}'s game", NamedTextColor.GREEN))
            return true
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String?>? {
        if (sender !is Player) {
            return null
        }
        if (args.size == 1) {
            val list = ArrayList<String?>()
            list.add("set")
            list.add("get")
            list.add("clear")
            return list
        } else if (args.size == 2) {
            return Bukkit.getOnlinePlayers().map { it.name }
        } else if (args.size == 3) {
            val gameList = ArrayList<String>()
            for (game in Game.entries) {
                gameList.add(game.name.lowercase(getDefault()))
            }
            return gameList
        }
        return null
    }
}