package me.softsheep6.arcadecore.games.listeners

import me.softsheep6.arcadecore.ArcadeCore
import me.softsheep6.arcadecore.games.Game
import me.softsheep6.arcadecore.games.GameUtils
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class ZeldaListeners(private val plugin: ArcadeCore) : Listener {

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val p = e.player
        if (GameUtils(plugin).getGame(p) != Game.ZELDA) return
        // don't autosmelt if sneaking
        if (p.isSneaking) return


        // WHY is there no tag that includes all ores are you serious????
        // i'm just gonna do it the stupid dumb way whatever
        val b = e.block
        val w = p.world
        val loc = b.location
        // oops gotta check for pickaxe too
        if (!b.blockData.isPreferredTool(p.inventory.itemInMainHand)) return

        when (b.type) {
            Material.IRON_ORE -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.IRON_INGOT))}
            Material.DEEPSLATE_IRON_ORE -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.IRON_INGOT))}
            Material.COPPER_ORE -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.COPPER_INGOT))}
            Material.DEEPSLATE_COPPER_ORE -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.COPPER_INGOT))}
            Material.GOLD_ORE -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.GOLD_INGOT))}
            Material.DEEPSLATE_GOLD_ORE -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.GOLD_INGOT))}
            Material.NETHER_GOLD_ORE -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.GOLD_INGOT))}
            Material.ANCIENT_DEBRIS -> {e.isDropItems = false; w.dropItemNaturally(loc, ItemStack(Material.NETHERITE_SCRAP))}
            else -> {}
        }
    }
}