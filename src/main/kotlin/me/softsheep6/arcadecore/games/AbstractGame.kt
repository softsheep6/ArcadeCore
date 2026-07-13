package me.softsheep6.arcadecore.games

import org.bukkit.entity.Player

abstract class AbstractGame {

    abstract fun abilityA(p: Player)

    abstract fun abilityB(p: Player)

    abstract fun passiveA()

    abstract fun passiveB()

}