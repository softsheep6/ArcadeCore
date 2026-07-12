package me.softsheep6.arcadecore

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin

class ArcadeCore : JavaPlugin(), Listener{

    override fun onEnable() {
        println("meow meow meow meow")
        Bukkit.getPluginManager().registerEvents(this, this)
    }
}