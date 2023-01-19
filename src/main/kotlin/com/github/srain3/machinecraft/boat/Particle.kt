package com.github.srain3.machinecraft.boat

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.util.Vector
import kotlin.math.PI

object Particle {
    fun brakeLamp(boatData: LandBoat, brake: Boolean) {
        val boat = boatData.getControlPlayer() ?: return
        val angle = -PI /180*(boat.location.yaw)
        val left = boat.location.clone().add(Vector(0.5,0.25,-1.05 + (boatData.speed.z / 2)).rotateAroundY(angle))
        val right = boat.location.clone().add(Vector(-0.5,0.25,-1.05 + (boatData.speed.z / 2)).rotateAroundY(angle))

        if (brake) {
            left.world?.spawnParticle(Particle.REDSTONE,left,1, Particle.DustOptions(Color.RED, 1.5F))
            right.world?.spawnParticle(Particle.REDSTONE,right,1, Particle.DustOptions(Color.RED, 1.5F))
        } else {
            left.world?.spawnParticle(Particle.REDSTONE,left,1, Particle.DustOptions(Color.RED, 0.5F))
            right.world?.spawnParticle(Particle.REDSTONE,right,1, Particle.DustOptions(Color.RED, 0.5F))
        }
    }
}