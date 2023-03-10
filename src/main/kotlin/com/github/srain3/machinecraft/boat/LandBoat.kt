package com.github.srain3.machinecraft.boat

import com.github.srain3.machinecraft.tools.ToolBox
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundMoveVehiclePacket
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BossBar
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.geysermc.geyser.GeyserImpl
import org.geysermc.geyser.translator.protocol.java.entity.JavaMoveVehicleTranslator
import kotlin.math.*

/**
 * 各ボートの情報
 */
data class LandBoat(
    val boat: Boat,
    val speed: Vector,
    val speedLimit: Double,
    val power: Double,
    val brakePower: Double,
    val bossBar: BossBar,
    var slipAngle: Float,
    val slipMomentum: Float,
    val distanceIsBoat: MutableMap<Player, Double>,
    var distanceVector: Vector,
    val item: ItemStack,
    var flySwitch: Boolean,
    val rental: Int?,
    var fee: Int,
    val placePlayer: Player
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
        if (onIce()) {
            bossBar.color = BarColor.BLUE
            bossBar.progress = 1.0
            bossBar.setTitle(ToolBox.colorMessage("Boat Speed: &9&lICE mode"))
        } else {
            val barPercent = speed.z / speedLimit
            var speedString = (speed.z * 50).roundToInt().toString()
            if (barPercent in -0.05..0.05) {
                bossBar.color = BarColor.WHITE
                speedString = "&r&l${speedString}&rkm/h"
            } else if (barPercent > 0.05) {
                bossBar.color = BarColor.GREEN
                speedString = "&a&l${speedString}&r&akm/h"
            } else if (barPercent < -0.05) {
                bossBar.color = BarColor.RED
                speedString = "&c&l${speedString}&r&ckm/h"
            }
            bossBar.progress = barPercent.absoluteValue

            if (rental != null) {
                bossBar.setTitle(ToolBox.colorMessage("Boat Speed: $speedString &r| Fee: &6${ToolBox.econ?.format(fee.toDouble())}"))
            } else {
                bossBar.setTitle(ToolBox.colorMessage("Boat Speed: $speedString"))
            }
        }
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
                val distance = distanceIsBoat[it]?.roundToInt() ?: 0
                it.sendMessage("今回の乗車距離: ${distance}m")
                if (rental != null) {
                    ToolBox.econ?.withdrawPlayer(it,fee.toDouble())
                    it.sendMessage("レンタル料金: ${ToolBox.econ?.format(fee.toDouble())}")
                }
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
                    if (rental != null) {
                        ToolBox.econ?.withdrawPlayer(it,fee.toDouble())
                        it.sendMessage("レンタル料金: ${ToolBox.econ?.format(fee.toDouble())}")
                    }
                    distanceIsBoat.remove(it)
                    distanceVector = boat.location.toVector()
                    bossBar.removePlayer(it)
                }
            } else {
                if (rental != null) {
                    ToolBox.econ?.withdrawPlayer(it,fee.toDouble())
                }
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
        val jumpSwitch = player.isFlying
        //player.sendMessage("$jumpSwitch")
        //Bukkit.getLogger().info(wasd)

        if (flySwitch != jumpSwitch) {
            player.sendMessage(ToolBox.colorMessage("[BoatCar] &aクルーズコントロールを&6${jumpSwitch}&aにしました。"))
            flySwitch = jumpSwitch
        }

        val addSpeed = Vector(0.0,0.0,1.0)

        if (!jumpSwitch) {
            when (wasd) { // 速度の判定
                "W", "WA", "WD" -> {
                    speed.multiply(0.965) // 摩擦的なやつ
                    addSpeed.multiply(
                        power * min(
                            1.0,
                            max(0.35, (speed.z * (0.038 - (speed.z * 0.0034)) / (power * (0.8 - power))))
                        )
                    )
                }

                "S", "SA", "SD" -> {
                    speed.multiply(0.965) // 摩擦的なやつ
                    addSpeed.multiply(
                        -brakePower * min(
                            1.0,
                            max(
                                0.35,
                                (speed.z.absoluteValue * (0.038 - (speed.z.absoluteValue * 0.0034)) / (brakePower * (0.8 - brakePower)))
                            )
                        )
                    )
                }

                else -> {
                    if (speed.z in -0.03..0.03) {
                        speed.multiply(0)
                    }
                    speed.multiply(0.9975) // 摩擦的なやつ
                    addSpeed.multiply(0)
                }
            }
            speed.add(addSpeed)
        }

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
                slipAngle = (slipAngle + slipMomentum + min(1.5F,speed.z.absoluteValue.toFloat()*1.2F)) * 0.899F
                if (wasd != "A") {
                    if (wasd == "SA") {
                        slipAngle *= 0.97F
                    }
                    if (!jumpSwitch) {
                        speed.multiply(0.9925)
                    }
                } else {
                    if (!jumpSwitch) {
                        speed.multiply(0.995)
                    }
                }
            }
            "D", "WD", "SD" -> {
                slipAngle = (slipAngle - slipMomentum - min(1.5F,speed.z.absoluteValue.toFloat()*1.2F)) * 0.899F
                if (wasd != "D") {
                    if (wasd == "SD") {
                        slipAngle *= 0.97F
                    }
                    if (!jumpSwitch) {
                        speed.multiply(0.9925)
                    }
                } else {
                    if (!jumpSwitch) {
                        speed.multiply(0.995)
                    }
                }
            }

            "W" -> {
                slipAngle *= min(0.975F,(slipMomentum/100.0F+0.9F))
            }

            "S" -> {
                slipAngle *= min(0.925F,(slipMomentum/100.0F+0.85F))
            }

            else -> {
                slipAngle *= 0.95F
            }
        }
        //player.sendMessage("Yaw: ${slipAngle[mount]}")
        when (wasd) {
            "S", "SA", "SD" -> {
                Particle.brakeLamp(this,true)
            }
            else -> {
                Particle.brakeLamp(this, false)
            }
        }

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
        var distance = distanceVector.distance(boat.location.toVector())

        if (rental != null) {
            if (onIce()) {
                distance = 0.0
            }
        }
        distanceIsBoat[player] = (distanceIsBoat[player] ?: 0.0) + distance
        distanceVector = boat.location.toVector()

        fee = (ToolBox.distanceFee * (distanceIsBoat[player] ?: 0.0).roundToInt() + (rental ?: 0)).roundToInt()
    }

    /**
     * 統合版対応コード
     */
    fun bedRockConvert(player: Player) {
        val session = GeyserImpl.getInstance().connectionByUuid(player.uniqueId)
        val pVec = player.velocity.clone()
        pVec.y = 0.0
        pVec.rotateAroundY(-PI /180*(-player.eyeLocation.yaw))
        pVec.multiply(200)
        val wasd = wasdKey(pVec)
        if (session != null) {
            val x = boat.location.x + (boat.velocity.x * 2.5)
            var y = boat.location.y + bedRockJump() + 0.001045
            val z = boat.location.z + (boat.velocity.z * 2.5)
            var yaw = 0F
            val pitch = boat.location.pitch

            when (wasd) {
                "A", "WA", "SA" -> {
                    yaw -= 7.5F
                }
                "D", "WD", "SD" -> {
                    yaw += 7.5F
                }
            }
            val downLoc = boat.location.clone()
            downLoc.y -= 1.0
            if (downLoc.block.isPassable) {
                y += boat.velocity.y
            }
            if (!boat.location.block.isPassable) {
                y += 0.1
            }
            //player.sendMessage(wasd)
            val bedrockBoat = session.entityCache.getEntityByJavaId(boat.entityId)
            bedrockBoat.updateRotation((boat.location.yaw.div(2.5).roundToInt().times(2.5).toFloat())+yaw,0F,bedrockBoat.isOnGround)
            JavaMoveVehicleTranslator().translate(session, ClientboundMoveVehiclePacket(x,y,z,bedrockBoat.yaw - 90F,pitch))

            //boat.location.yaw = yaw
        }
    }

    /**
     * 統合版でボートを登らせる
     */
    private fun bedRockJump(): Double {
        val player = getControlPlayer() ?: return 0.0
        val selectVec = Vector(0.0,0.0,1.0).rotateAroundY(-PI /180*player.eyeLocation.yaw)

        val boatLoc = boat.location
        val rtb = boat.world.rayTraceBlocks(boatLoc, selectVec, 2.0) ?: return 0.0
        val hitBlock = rtb.hitBlock ?: return 0.0

        if (!(hitBlock.type == Material.ICE || hitBlock.type == Material.BLUE_ICE || hitBlock.type == Material.FROSTED_ICE || hitBlock.type == Material.PACKED_ICE)) {
            if (hitBlock.isPassable) return 0.0
            val upBlock = hitBlock.location.add(0.0,1.0,0.0).block
            if (!upBlock.isEmpty) {
                if (!upBlock.isPassable) return 0.0
            }
            if (boat.velocity.x in -0.02..0.02 && boat.velocity.z in -0.02..0.02) return 0.0
            //boat.velocity = boat.velocity.add(Vector(0.0,1.175,0.0))
            return 1.205
        } else {
            //boat.velocity = boat.velocity.setY(0.275)
            return 0.275
        }
    }
}
