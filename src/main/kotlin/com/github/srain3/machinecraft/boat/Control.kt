package com.github.srain3.machinecraft.boat

import org.bukkit.Material
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.min

object Control {

    /**
     * ボートの速度変数
     */
    val boatSpeed = mutableMapOf<Boat, Vector>()
    private val slipAngle = mutableMapOf<Boat, Float>()
    private const val speedLimit = 1.5

    /**
     * ボートを段差で登らせる
     */
    fun jump(player: Player, mount: Boat): Boolean {
        val selectVec = Vector(0.0,0.0,1.0).rotateAroundY(-PI /180*player.eyeLocation.yaw)
        val rtb = mount.world.rayTraceBlocks(mount.location, selectVec, 2.0) ?: return false
        val hitBlock = rtb.hitBlock ?: return false
        if (!(hitBlock.type == Material.ICE || hitBlock.type == Material.BLUE_ICE || hitBlock.type == Material.FROSTED_ICE || hitBlock.type == Material.PACKED_ICE)) {
            if (hitBlock.isPassable) return false
            val upBlock = hitBlock.location.add(0.0,1.0,0.0).block
            if (!upBlock.isEmpty) {
                if (!upBlock.isPassable) return false
            }
            if (mount.velocity.x in -0.02..0.02 && mount.velocity.z in -0.02..0.02) return false
            mount.velocity = mount.velocity.add(Vector(0.0,1.175,0.0))
        } else {
            mount.velocity = mount.velocity.setY(0.275)
        }

        return true
    }

    /**
     * ボートの速度を変更
     */
    fun speedAdd(player: Player, mount: Boat) {
        val pVec = player.velocity.clone()
        pVec.y = 0.0
        pVec.rotateAroundY(-PI /180*(-player.eyeLocation.yaw))
        pVec.multiply(200)
        val wasd = wasdKey(pVec)
        //Bukkit.getLogger().info(wasd)

        val addSpeed = Vector(0.0,0.0,1.0)
        val speed = boatSpeed[mount] ?: return
        speed.multiply(0.965) // 摩擦的なやつ
        when (wasd) { // 速度の判定
            "W", "WA", "WD" -> {
                addSpeed.multiply(0.09)
            }
            "S", "SA", "SD" -> {
                addSpeed.multiply(-0.05)
            }
            else -> {
                if (speed.z in -0.03..0.03) {
                    speed.multiply(0)
                }
                addSpeed.multiply(0)
            }
        }

        speed.add(addSpeed)
        if (speed.z >= speedLimit) {
            speed.z = speedLimit
        } else if (speed.z <= -(speedLimit*0.4)) {
            speed.z = -(speedLimit*0.4)
        }
        speed.y = -0.61
        //Bukkit.getLogger().info("$oldVec")

        // スリップ(滑る)または曲がる挙動用
        val newYaw = when (wasd) {
            "A", "WA", "SA" -> {
                val slip = ((slipAngle[mount] ?: 0F) + (2.5F*(0.5F+min(1.0F,speed.z.absoluteValue.toFloat())))) * 0.9F
                slipAngle[mount] = slip
                if (wasd != "A") {
                    speed.multiply(0.9825)
                } else {
                    speed.multiply(0.975)
                }
                mount.location.yaw + slip
            }
            "D", "WD", "SD" -> {
                val slip = ((slipAngle[mount] ?: 0F) - (2.5F*(0.5F+ min(1.0F,speed.z.absoluteValue.toFloat())))) * 0.9F
                slipAngle[mount] = slip
                if (wasd != "D") {
                    speed.multiply(0.9825)
                } else {
                    speed.multiply(0.975)
                }
                mount.location.yaw + slip
            }

            "W", "S" -> {
                slipAngle[mount] = (slipAngle[mount]?: 0F) * (0.4775F*(speed.z.absoluteValue.toFloat()/speedLimit.toFloat()+1.0F))
                mount.location.yaw + (slipAngle[mount] ?: 0F)
            }

            else -> {
                slipAngle[mount] = (slipAngle[mount]?: 0F) * 0.95F
                mount.location.yaw + (slipAngle[mount] ?: 0F)
            }
        }
        //player.sendMessage("Yaw: ${slipAngle[mount]}")

        mount.velocity = speed.clone().rotateAroundY(-PI /180*newYaw)
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