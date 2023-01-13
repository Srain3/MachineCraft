package com.github.srain3.machinecraft.boat

import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BossBar
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 各ボートの情報
 */
data class LandBoat(
    val boat: Boat,
    val speed: Vector,
    val speedLimit: Double,
    val bossBar: BossBar,
    var slipAngle: Float,
    val distanceIsBoat: MutableMap<Player, Double>,
    var distanceVector: Vector
) {
    /**
     * 操縦者を取得
     */
    fun getControlPlayer(): Player? {
        return boat.passengers.filterIsInstance<Player>().getOrNull(0)
    }

    /**
     * 乗車しているプレイヤーを全取得
     */
    private fun getMountPlayer(): List<Player> {
        return boat.passengers.filterIsInstance<Player>()
    }

    /**
     * メーター更新
     */
    fun updateBar() {
        val barPercent = speed.z / speedLimit
        if (barPercent in -0.05..0.05) {
            bossBar.color = BarColor.WHITE
        } else if (barPercent > 0.05){
            bossBar.color = BarColor.GREEN
        } else if (barPercent < -0.05) {
            bossBar.color = BarColor.RED
        }
        bossBar.progress = barPercent.absoluteValue
    }

    /**
     * プレイヤーごとのボスバー設定
     */
    fun updatePlayer() {
        val players = getMountPlayer()
        players.forEach {
            if (!bossBar.players.contains(it)) {
                bossBar.addPlayer(it)
                distanceVector = boat.location.toVector()
                distanceIsBoat[it] = distanceIsBoat[it] ?: 0.0
            }
        }
        if (players.isEmpty()) {
            bossBar.players.forEach {
                it.sendMessage("今回の乗車距離: ${distanceIsBoat[it]?.roundToInt()}m")
                distanceIsBoat.remove(it)
                distanceVector = boat.location.toVector()
            }
            bossBar.removeAll()
            return
        }
        bossBar.players.forEach {
            if (it.isOnline) {
                if (!players.contains(it)) {
                    it.sendMessage("今回の乗車距離: ${distanceIsBoat[it]?.roundToInt()}m")
                    distanceIsBoat.remove(it)
                    distanceVector = boat.location.toVector()
                    bossBar.removePlayer(it)
                }
            } else {
                distanceIsBoat.remove(it)
                distanceVector = boat.location.toVector()
                bossBar.removePlayer(it)
            }
        }
    }

    /**
     * ボートを段差で登らせる
     */
    fun jump(): Boolean {
        val player = getControlPlayer() ?: return false
        val selectVec = Vector(0.0,0.0,1.0).rotateAroundY(-PI /180*player.eyeLocation.yaw)

        val rtb = boat.world.rayTraceBlocks(boat.location, selectVec, 2.0) ?: return false
        val hitBlock = rtb.hitBlock ?: return false

        if (!(hitBlock.type == Material.ICE || hitBlock.type == Material.BLUE_ICE || hitBlock.type == Material.FROSTED_ICE || hitBlock.type == Material.PACKED_ICE)) {
            if (hitBlock.isPassable) return false
            val upBlock = hitBlock.location.add(0.0,1.0,0.0).block
            if (!upBlock.isEmpty) {
                if (!upBlock.isPassable) return false
            }
            if (boat.velocity.x in -0.02..0.02 && boat.velocity.z in -0.02..0.02) return false
            boat.velocity = boat.velocity.add(Vector(0.0,1.175,0.0))
        } else {
            boat.velocity = boat.velocity.setY(0.275)
        }

        return true
    }

    /**
     * ボートの速度を変更
     */
    fun speedAdd() {
        val player = getControlPlayer() ?: return
        val pVec = player.velocity.clone()
        pVec.y = 0.0
        pVec.rotateAroundY(-PI /180*(-player.eyeLocation.yaw))
        pVec.multiply(200)
        val wasd = wasdKey(pVec)
        //Bukkit.getLogger().info(wasd)

        val addSpeed = Vector(0.0,0.0,1.0)
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
        } else if (speed.z <= -(speedLimit *0.4)) {
            speed.z = -(speedLimit *0.4)
        }
        speed.y = -0.61
        //Bukkit.getLogger().info("$oldVec")

        // スリップ(滑る)または曲がる挙動用
        when (wasd) {
            "A", "WA", "SA" -> {
                slipAngle = (slipAngle + (2.5F*(0.5F+ min(1.0F,speed.z.absoluteValue.toFloat())))) * 0.9F
                if (wasd != "A") {
                    speed.multiply(0.9825)
                } else {
                    speed.multiply(0.975)
                }
            }
            "D", "WD", "SD" -> {
                slipAngle = (slipAngle - (2.5F*(0.5F+ min(1.0F,speed.z.absoluteValue.toFloat())))) * 0.9F
                if (wasd != "D") {
                    speed.multiply(0.9825)
                } else {
                    speed.multiply(0.975)
                }
            }

            "W", "S" -> {
                slipAngle *= (0.4775F * (speed.z.absoluteValue.toFloat() / speedLimit.toFloat() + 1.0F))
            }

            else -> {
                slipAngle *= 0.95F
            }
        }
        //player.sendMessage("Yaw: ${slipAngle[mount]}")

        boat.velocity = speed.clone().rotateAroundY(-PI /180*(boat.location.yaw+slipAngle))
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

    /**
     * 氷系Blockリスト
     */
    private val iceList = listOf(
        Material.ICE,
        Material.PACKED_ICE,
        Material.BLUE_ICE,
        Material.FROSTED_ICE
    )

    /**
     * 氷系Blockの上を走っているかどうか
     */
    fun onIce(): Boolean {
        val downLoc = boat.location.clone()
        downLoc.y -= 0.55
        val type = downLoc.block.type
        return iceList.contains(type)
    }

    /**
     * ブロックに当たったら減速
     */
    fun blockHitCheck() {
        val frontVec = Vector(0.0,0.0,1.0).rotateAroundY(-PI /180*boat.location.yaw)
        val frontResult = boat.world.rayTraceBlocks(boat.location, frontVec, 0.75)
        val leftVec = Vector(-1.0,0.0,1.0).rotateAroundY(-PI /180*boat.location.yaw)
        val leftResult = boat.world.rayTraceBlocks(boat.location, leftVec, 0.9)
        val rightVec = Vector(1.0,0.0,1.0).rotateAroundY(-PI /180*boat.location.yaw)
        val rightResult = boat.world.rayTraceBlocks(boat.location, rightVec, 0.9)

        if (!(frontResult == null && leftResult == null && rightResult == null)) {
            val frontPass = frontResult?.hitBlock?.isPassable ?: true
            val leftPass = leftResult?.hitBlock?.isPassable ?: true
            val rightPass = rightResult?.hitBlock?.isPassable ?: true
            if (!(frontPass && leftPass && rightPass)) {
                speed.multiply(0.65)
            }
        }
    }

    /**
     * 距離測定
     */
    fun distanceUpdate() {
        val player = getControlPlayer() ?: return
        val distance = distanceVector.distance(boat.location.toVector())
        distanceIsBoat[player] = (distanceIsBoat[player] ?: 0.0) + distance
        distanceVector = boat.location.toVector()
    }
}