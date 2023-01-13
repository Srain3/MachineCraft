package com.github.srain3.machinecraft.events

import com.github.srain3.machinecraft.boat.LandBoat
import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Boat
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.spigotmc.event.entity.EntityMountEvent

/**
 * 乗車イベント
 */
object RideEvent: Listener {

    val boatList = mutableListOf<LandBoat>()

    /**
     * ボートに乗るイベントキャッチ
     */
    @EventHandler
    fun getBoatRide(event: EntityMountEvent) {
        //Bukkit.getLogger().info("entity:${event.entity.type.name} mount:${event.mount.type.name}")
        val mount = event.mount
        if (mount !is Boat) return
        val boatList = boatList.filter { it.boat.uniqueId == mount.uniqueId }
        if (boatList.isEmpty()) return
        if (mount.location.block.type == Material.WATER) return // 水の場合はキャンセル
        //Bukkit.getLogger().info("Mount is NewBoat!")

        val boatData = boatList.first()

        object : BukkitRunnable() {
            override fun run() {
                if (boatData.boat.isDead) {
                    //boatData.bossBar.removeAll()
                    //Bukkit.removeBossBar(ToolBox.pluginNamespaceKey(boatData.boat.uniqueId.toString()))
                    //RideEvent.boatList.remove(boatData)
                    cancel()
                    return
                }
                //mount.world.playSound(mount.location, Sound.BLOCK_LAVA_POP, SoundCategory.AMBIENT, 0.6F, 0.515F)

                val player = boatData.getControlPlayer()

                if (player == null) {
                    boatData.speed.zero()
                    boatData.updateBar()
                    boatData.updatePlayer()
                    return
                }

                if (boatData.onIce()) {
                    // 氷の上
                    boatData.jump()
                    boatData.speed.zero()
                } else {
                    // それ以外
                    boatData.speedAdd()

                    boatData.updateBar()
                    boatData.updatePlayer()

                    if (!boatData.jump()) {
                        boatData.blockHitCheck()
                    }
                }
                boatData.distanceUpdate()

                return
            }
        }.runTaskTimer(ToolBox.pl, 1, 1)
    }

    private val boatRegex = Regex(""".*_BOAT""")
    private val topSpeedRegex = Regex("""TopSpeed: [0-9]+""")
    private val powerRegex = Regex("""Power: [0-9]+""")
    private val brakeRegex = Regex("""Brake: [0-9]+""")

    @Suppress("DEPRECATION")
    @EventHandler
    fun boatEntitySpawn(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.isCancelled) return
        val item = event.item?.clone() ?: return
        if (!boatRegex.matches(item.type.name)) return
        val meta = item.itemMeta ?: return
        val clickLoc = event.clickedBlock?.location?.clone() ?: return
        clickLoc.y += 1.0

        var topSpeedInt = 50
        var powerInt = 40
        var brakeInt = 20
        meta.lore?.forEach { line ->
            if (topSpeedRegex.matches(line)) {
                // 最高速設定
                topSpeedInt = line.replace("TopSpeed: ","").toInt()
            } else if (powerRegex.matches(line)) {
                // エンジンパワー設定(加速力)
                powerInt = line.replace("Power: ","").toInt()
            } else if (brakeRegex.matches(line)) {
                // ブレーキ力(減速力)
                brakeInt = line.replace("Brake: ","").toInt()
            }
        }

        object : BukkitRunnable() {
            override fun run() {
                val boatList = clickLoc.world?.getNearbyEntities(clickLoc,0.4,0.4,0.4) {
                    it is Boat
                } ?: return
                boatList.forEach { boat ->
                    if (boat !is Boat) return@forEach

                    val bossBar = Bukkit.createBossBar(ToolBox.pluginNamespaceKey(boat.uniqueId.toString()),"Boat Speed",BarColor.WHITE,BarStyle.SOLID)
                    val topSpeed = topSpeedInt * 0.02
                    val power = powerInt * 0.001
                    val brake = brakeInt * 0.001

                    val boatData = LandBoat(boat,Vector(),topSpeed,power,brake,bossBar,0F, mutableMapOf(), boat.location.toVector(), item.clone())
                    RideEvent.boatList.add(boatData)
                }
            }
        }.runTaskLater(ToolBox.pl, 1)
    }

    @EventHandler
    fun deathBoat(event: VehicleDestroyEvent) {
        if (event.vehicle !is Boat) return
        val boatData = boatList.firstOrNull { it.boat.uniqueId == event.vehicle.uniqueId } ?: return
        val boatLoc = event.vehicle.location.clone()

        boatData.bossBar.removeAll()
        Bukkit.removeBossBar(ToolBox.pluginNamespaceKey(boatData.boat.uniqueId.toString()))

        object : BukkitRunnable() {
            override fun run() {
                val itemEntity = boatLoc.world?.getNearbyEntities(boatLoc,0.3,0.3,0.3) {
                    it is Item
                } ?: return
                val entity = itemEntity.firstOrNull()
                if (entity != null) {
                    if (entity !is Item) return
                    if (boatRegex.matches(entity.itemStack.type.name)) {
                        entity.itemStack = boatData.item
                    }
                }
                boatList.remove(boatData)
            }
        }.runTaskLater(ToolBox.pl, 1)
    }
}