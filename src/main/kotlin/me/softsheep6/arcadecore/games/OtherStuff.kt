package me.softsheep6.arcadecore.games

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.cos
import kotlin.math.sin

class OtherStuff {

    // i just know im gonna use this circle method at least a few times so here it gets its own class unless i add other things here too idk
    // taken from the plugin i made for jplays squid games which was also taken from uhhh idk some random stack overflow post
    // but i converted it into kotlin so yea im slightly less of a skid (very very slightly)
    fun getPoints(x0: Double, y: Double, z0: Double, r: Int, noOfDividingPoints: Int, world: World): List<Location> {
        var angle: Double

        val xyCoords = ArrayList<Double>(noOfDividingPoints)
        val zCoords = ArrayList<Double>(noOfDividingPoints)
        val locations = ArrayList<Location>(noOfDividingPoints)

        for (i in 0..<noOfDividingPoints) {
            angle = i * (360.0/noOfDividingPoints)

            xyCoords.add(x0 + r * cos(Math.toRadians(angle)))
            zCoords.add(z0 + r * sin(Math.toRadians(angle)))

            locations.add(Location(world, xyCoords[i], y, zCoords[i]))
        }
        return locations
    }
}