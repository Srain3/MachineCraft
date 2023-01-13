package com.github.srain3.machinecraft.events

import org.bukkit.entity.Boat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent

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

        boatData.speed.multiply(0.5)
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