package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameUtils
import me.softsheep6.arcadecore.games.abilities.SpiderMan
import me.softsheep6.arcadecore.games.listeners.SpiderManListeners.Foo.blocks
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import kotlin.math.cos
import kotlin.math.sin

class SpiderManListeners(private val plugin: ArcadeCore) : Listener {

    object Foo {
        val blocks = ArrayList<Block>() // to keep track of cobweb removal

        // BELOW 2 FUNCTIONS TAKEN FROM https://www.spigotmc.org/threads/allowing-a-player-to-climb-up-walls.529517/#post-4281854 AND CONVERTED TO KOTLIN BY ME
        fun checkClimbing(p: Player): Boolean {
            val blocks = HashSet<Block>(checkBlock(p, -25, 25, 0.8))
            blocks.forEach {
                if (it.type.isSolid()) {
                    return true
                }
            }
            return false
        }
        fun checkBlock(player: Player, min: Int, max: Int, distance: Double): Set<Block> {
            val blocks = HashSet<Block>()
            val origin = player.location
            for (i in min..<max step 5) {
                val angle = Math.toRadians((player.location.yaw + 90 + i).toDouble())
                val x = origin.x + (distance * cos(angle))
                val z = origin.z + (distance * sin(angle))
                blocks.add(Location(player.world, x, origin.y, z).block)
            }
            return blocks
        }
    }

    // no breaking the Cobweb Cage
    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (e.block.type == Material.COBWEB && blocks.contains(e.block)) e.isCancelled = true
    }

    // wall climbing
    @EventHandler
    fun onPlayerSneak(e: PlayerToggleSneakEvent) {
        val p = e.getPlayer()
        if (e.isSneaking && GameUtils(plugin).getGame(p) == Game.SPIDERMAN) SpiderMan(plugin).passiveB(p)

    }   
}