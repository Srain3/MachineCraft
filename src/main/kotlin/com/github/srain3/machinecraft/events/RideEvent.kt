package com.github.srain3.machinecraft.events

import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.spigotmc.event.entity.EntityMountEvent
import kotlin.math.PI

/**
 * 乗車イベント
 */
object RideEvent: Listener {
    @EventHandler
    fun getRide(event: EntityMountEvent) {
        //Bukkit.getLogger().info("entity:${event.entity.type.name} mount:${event.mount.type.name}")
        val mount = event.mount
        if (mount !is Boat) return
        if (boatList.contains(mount)) return
        if (mount.location.block.type == Material.WATER) return
        //Bukkit.getLogger().info("Mount is NewBoat!")
        boatList[mount] = Vector(0.0,0.0,0.0)

        object : BukkitRunnable() {
            override fun run() {
                if (mount.isDead) {
                    boatList.remove(mount)
                    cancel()
                    return
                }
                //mount.world.playSound(mount.location, Sound.BLOCK_LAVA_POP, SoundCategory.AMBIENT, 0.6F, 0.515F)

                val player = mount.passengers.filterIsInstance<Player>().getOrNull(0)

                if (player == null) {
                    boatList[mount] = Vector(0.0,0.0,0.0)
                    return
                }

                speedAdd(player, mount)
                jump(player, mount)

                //Bukkit.getLogger().info("Jump")
                return
            }
        }.runTaskTimer(ToolBox.pl, 1, 1)
    }

    private val boatList = mutableMapOf<Boat, Vector>()

    private fun jump(player: Player, mount: Boat) {
        val selectVec = Vector(0.0,0.0,1.0).rotateAroundY(-PI/180*player.eyeLocation.yaw)
        val rtb = mount.world.rayTraceBlocks(mount.location, selectVec, 2.0) ?: return
        val hitBlock = rtb.hitBlock ?: return
        val yVec = Vector(0.0,0.675,0.0)
        if (!(hitBlock.type == Material.ICE || hitBlock.type == Material.BLUE_ICE || hitBlock.type == Material.FROSTED_ICE || hitBlock.type == Material.PACKED_ICE)) {
            if (hitBlock.isPassable) return
            val upBlock = hitBlock.location.add(0.0,1.0,0.0).block
            if (!upBlock.isEmpty) return
            if (!upBlock.isPassable) return
            if (mount.velocity.x in -0.02..0.02 && mount.velocity.z in -0.02..0.02) return
        } else {
            yVec.y -= 0.25
        }

        mount.velocity = mount.velocity.add(yVec)//.add(selectVec.multiply(0.625))
    }

    private const val speedLimit = 1.5

    private fun speedAdd(player: Player, mount: Boat) {
        val pVec = player.velocity.clone()
        pVec.y = 0.0
        pVec.rotateAroundY(-PI/180*(-player.eyeLocation.yaw))
        pVec.multiply(200)
        val wasd = wasdKey(pVec)
        Bukkit.getLogger().info(wasd)

        val mVec = Vector(0.0,0.0,1.0)
        mVec.rotateAroundY(-PI/180*mount.location.yaw)
        val oldVec = boatList[mount] ?: return
        oldVec.multiply(0.965)
        when (wasd) {
            "W", "WA", "WD" -> {
                mVec.multiply(0.09)
            }
            "S", "SA", "SD" -> {
                mVec.multiply(-0.05)
            }
            else -> {
                if (oldVec.x in -0.01..0.01 && oldVec.z in -0.01..0.01) {
                    oldVec.multiply(0)
                }
                mVec.multiply(0)
            }
        }
        oldVec.add(mVec)
        if (oldVec.x >= speedLimit) {
            oldVec.x = speedLimit
        } else if (oldVec.x <= -speedLimit) {
            oldVec.x = -speedLimit
        }
        if (oldVec.z >= speedLimit) {
            oldVec.z = speedLimit
        } else if (oldVec.z <= -speedLimit) {
            oldVec.z = -speedLimit
        }
        oldVec.y = -0.21
        Bukkit.getLogger().info("$oldVec")
        mount.velocity = oldVec
    }

    private fun wasdKey(pVec: Vector): String {
        return when {
            pVec.z >= 1.0 -> { // W
                when {
                    pVec.x >= 1.0 -> { // A
                        "WA"
                    }
                    pVec.x <= -1.0 -> { // D
                        "WD"
                    }
                    else -> {
                        "W"
                    }
                }
            }
            pVec.z <= -1.0 -> { // S
                when {
                    pVec.x >= 1.0 -> { // A
                        "SA"
                    }
                    pVec.x <= -1.0 -> { // D
                        "SD"
                    }
                    else -> {
                        "S"
                    }
                }
            }
            else -> {
                when {
                    pVec.x >= 1.0 -> { // A
                        "A"
                    }
                    pVec.x <= -1.0 -> { // D
                        "D"
                    }
                    else -> {
                        ""
                    }
                }
            }
        }
    }
}