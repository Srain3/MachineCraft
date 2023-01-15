package com.github.srain3.machinecraft.events

import org.bukkit.entity.Boat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent
import kotlin.math.PI

object VehicleMove: Listener {

    //private val hitList = mutableMapOf<Boat, MutableMap<UUID, LocalDateTime>>()

    /**
     * エンティティにあたったら通知と減速
     */
    @EventHandler
    fun entityHitEvent(event: VehicleEntityCollisionEvent) {
        if (event.vehicle !is Boat) return
        val eventBoat = event.vehicle as Boat
        val boatData = RideEvent.boatList.firstOrNull { it.boat.uniqueId == eventBoat.uniqueId } ?: return

        if (boatData.speed.z !in -0.05..0.05) {
            val hitVec = boatData.speed.clone().multiply(1.75).rotateAroundY(-PI / 180 * boatData.boat.location.yaw)
            if (event.entity is Boat) {
                hitVec.y = 0.0
                boatData.speed.multiply(0.975)
            } else {
                hitVec.y = 0.5
                boatData.speed.multiply(0.75)
            }

            event.entity.velocity = event.entity.velocity.add(hitVec)
        }
        /* エンティティヒット通知イベント
        val entity = event.entity
        if (entity !is LivingEntity) return
        val player = boatData.getControlPlayer() ?: return
        if (hitList[boatData.boat]?.get(entity.uniqueId) == null) {
            hitList[boatData.boat] = (hitList[boatData.boat]?: mutableMapOf()).apply { set(entity.uniqueId, LocalDateTime.now()) }
            player.server.onlinePlayers.forEach {
                it.sendMessage(ToolBox.colorMessage("[&c事故発生!&r] ${player.name} が${entity.name}と衝突しました!"))
            }
        } else if (hitList[boatData.boat]?.get(entity.uniqueId)?.plusSeconds(15)?.isBefore(LocalDateTime.now()) == true) {
            (hitList[boatData.boat]?: mutableMapOf()).remove(entity.uniqueId)
            hitList[boatData.boat] = (hitList[boatData.boat]?: mutableMapOf()).apply { set(entity.uniqueId, LocalDateTime.now()) }
            player.server.onlinePlayers.forEach {
                it.sendMessage(ToolBox.colorMessage("[&c事故発生!&r] ${player.name} が${entity.name}と衝突しました!"))
            }
        }
        */
    }

}