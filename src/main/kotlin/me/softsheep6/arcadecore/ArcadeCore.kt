package me.softsheep6.arcadecore

import me.softsheep6.arcadecore.commands.AbilityCommand
import me.softsheep6.arcadecore.commands.GameCommand
import me.softsheep6.arcadecore.games.CooldownManager
import me.softsheep6.arcadecore.games.GameUtils
import me.softsheep6.arcadecore.games.PassiveManager
import me.softsheep6.arcadecore.games.listeners.BendyListeners
import me.softsheep6.arcadecore.games.listeners.HollowKnightListeners
import me.softsheep6.arcadecore.games.listeners.MarioListeners
import me.softsheep6.arcadecore.games.listeners.ZeldaListeners
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class ArcadeCore : JavaPlugin(), Listener {


    override fun onEnable() {

        this.logger.info("ArcadeCore plugin enabled !!!")

        getCommand("ability")!!.setExecutor(AbilityCommand(this))
        getCommand("game")!!.setExecutor(GameCommand(this))
        Bukkit.getPluginManager().registerEvents(GameUtils(this), this)
        Bukkit.getPluginManager().registerEvents(CooldownManager(this), this)
        Bukkit.getPluginManager().registerEvents(PassiveManager(this), this)
        Bukkit.getPluginManager().registerEvents(BendyListeners(this), this)
        Bukkit.getPluginManager().registerEvents(HollowKnightListeners(), this)
        Bukkit.getPluginManager().registerEvents(MarioListeners(this), this)
        Bukkit.getPluginManager().registerEvents(ZeldaListeners(this), this)





        // times used google ai overview: 1
        // for every time i use ai overview i will slap myself in the face with a pillow shaped cinder block 20 times in a row 👍
    }

}