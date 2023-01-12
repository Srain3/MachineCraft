package com.github.srain3.machinecraft.boat

import com.github.srain3.machinecraft.boat.Control.boatSpeed
import org.bukkit.boss.BarColor
import org.bukkit.boss.BossBar
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object SpeedBar {

    val boatSpeedBar = mutableMapOf<Boat, BossBar>()
    private val distanceIsBoat = mutableMapOf<Player, Double>()
    private val vectorList = mutableMapOf<Player, Vector>()

    /**
     * メーター更新
     */
    fun updateBar(mount: Boat) {
        val boatVec = boatSpeed[mount]?: return
        val barPercent = boatVec.z / 1.5
        val bossBar = boatSpeedBar[mount] ?: return
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
    fun updatePlayer(mount: Boat, players: List<Player>) {
        val oldBarPlayers = boatSpeedBar[mount]?.players ?: return
        players.forEach {
                if (!oldBarPlayers.contains(it)) {
                    boatSpeedBar[mount]?.addPlayer(it)
                    vectorList[it] = it.location.toVector()
                    distanceIsBoat[it] = distanceIsBoat[it] ?: 0.0
                }
        }
        if (players.isEmpty()) {
            oldBarPlayers.forEach {
                it.sendMessage("今回の乗車距離: ${distanceIsBoat[it]?.roundToInt()}m")
                distanceIsBoat.remove(it)
                vectorList.remove(it)
            }
            boatSpeedBar[mount]?.removeAll()
            return
        }
        oldBarPlayers.forEach {
            if (it.isOnline) {
                if (!players.contains(it)) {
                    it.sendMessage("今回の乗車距離: ${distanceIsBoat[it]?.roundToInt()}m")
                    distanceIsBoat.remove(it)
                    vectorList.remove(it)
                    boatSpeedBar[mount]?.removePlayer(it)
                }
            } else {
                it.sendMessage("今回の乗車距離: ${distanceIsBoat[it]?.roundToInt()}m")
                distanceIsBoat.remove(it)
                vectorList.remove(it)
                boatSpeedBar[mount]?.removePlayer(it)
            }
        }
    }

    /**
     * 距離測定
     */
    fun distanceUpdate(player: Player) {
        val oldVec = vectorList[player]?: player.location.toVector()
        val distance = oldVec.distance(player.location.toVector())
        distanceIsBoat[player] = (distanceIsBoat[player] ?: 0.0) + distance
        vectorList[player] = player.location.toVector()
    }
}