package com.github.srain3.machinecraft.events

import com.github.srain3.machinecraft.boat.Control.boatSpeed
import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.entity.Boat
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent
import org.bukkit.util.Vector
import java.time.LocalDateTime
import java.util.*
import kotlin.math.PI

object VehicleMove: Listener {

    val hitList = mutableMapOf<Boat, MutableMap<UUID, LocalDateTime>>()

    /**
     * エンティティにあたったら通知と減速
     */
    @EventHandler
    fun entityHitEvent(event: VehicleEntityCollisionEvent) {
        if (event.vehicle !is Boat) return
        val boat = event.vehicle as Boat
        val speedVec = boatSpeed[boat] ?: return

        boatSpeed[boat] = speedVec.multiply(0.5)
        /*
        val entity = event.entity
        if (entity !is LivingEntity) return
        val player = boat.passengers.filterIsInstance<Player>().firstOrNull() ?: return
        if (hitList[boat]?.get(entity.uniqueId) == null) {
            hitList[boat] = (hitList[boat]?: mutableMapOf()).apply { set(entity.uniqueId, LocalDateTime.now()) }
            player.server.onlinePlayers.forEach {
                it.sendMessage(ToolBox.colorMessage("[&c事故発生!&r] ${player.name} が${entity.name}と衝突しました!"))
            }
        } else if (hitList[boat]?.get(entity.uniqueId)?.plusSeconds(15)?.isBefore(LocalDateTime.now()) == true) {
            (hitList[boat]?: mutableMapOf()).remove(entity.uniqueId)
            hitList[boat] = (hitList[boat]?: mutableMapOf()).apply { set(entity.uniqueId, LocalDateTime.now()) }
            player.server.onlinePlayers.forEach {
                it.sendMessage(ToolBox.colorMessage("[&c事故発生!&r] ${player.name} が${entity.name}と衝突しました!"))
            }
        }
         */
    }

    /**
     * ブロックに当たったら減速
     */
    fun blockHitCheck(boat: Boat) {
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
                boatSpeed[boat] = (boatSpeed[boat] ?: Vector()).multiply(0.65)
            }
        }
    }

}