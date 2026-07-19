package me.softsheep6.arcadecore.commands

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameManager
import me.softsheep6.arcadecore.games.abilities.Bendy
import me.softsheep6.arcadecore.games.abilities.HollowKnight
import me.softsheep6.arcadecore.games.abilities.Mario
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AbilityCommand(private val plugin: ArcadeCore) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (sender !is Player) {
            return true
        }

        val p = sender
         if (!(GameManager(plugin).hasGame(p))) {
          p.sendMessage(Component.text("You do not have a Game yet!").color(NamedTextColor.RED))
          return true
         }
        if (args.isEmpty() || args[0].isEmpty() || args.size > 1) {
            p.sendMessage(Component.text("Usage: /ability <a/b>").color(NamedTextColor.RED))
            return true
        }


        // activate ability depending on what game the player has equipped
        if (args[0].equals("a", ignoreCase = true)) {
            when (GameManager(plugin).getGame(p)) {
                Game.BENDY -> Bendy(plugin).abilityA(p)
                Game.HOLLOW_KNIGHT -> HollowKnight(plugin).abilityA(p)
                Game.MARIO -> Mario(plugin).abilityA(p)
                Game.ZELDA -> TODO()
                Game.CASTLE_CRASHERS -> TODO()
                Game.VALORANT -> TODO()
                Game.SPIDERMAN -> TODO()
                Game.DONKEY_KONG -> TODO()
                Game.POKEMON -> TODO()
                Game.SONIC -> TODO()
                Game.RYU -> TODO()
                Game.SUBNAUTICA -> TODO()
                Game.AMONG_US -> TODO()
                Game.CUPHEAD -> TODO()
                Game.NONE -> p.sendMessage(Component.text("You do not have a Game yet!").color(NamedTextColor.RED))
            }
            return true
        } else if (args[0].equals("b", ignoreCase = true)) {
            when (GameManager(plugin).getGame(p)) {
                Game.BENDY -> Bendy(plugin).abilityB(p)
                Game.HOLLOW_KNIGHT -> HollowKnight(plugin).abilityB(p)
                Game.MARIO -> Mario(plugin).abilityB(p)
                Game.ZELDA -> TODO()
                Game.CASTLE_CRASHERS -> TODO()
                Game.VALORANT -> TODO()
                Game.SPIDERMAN -> TODO()
                Game.DONKEY_KONG -> TODO()
                Game.POKEMON -> TODO()
                Game.SONIC -> TODO()
                Game.RYU -> TODO()
                Game.SUBNAUTICA -> TODO()
                Game.AMONG_US -> TODO()
                Game.CUPHEAD -> TODO()
                Game.NONE -> p.sendMessage(Component.text("You do not have a Game yet!").color(NamedTextColor.RED))
            }
            return true
        }


        p.sendMessage(Component.text("Usage: /ability <a/b>").color(NamedTextColor.RED))
        return true
    }
}