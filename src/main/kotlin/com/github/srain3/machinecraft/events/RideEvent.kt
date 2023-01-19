package com.github.srain3.machinecraft.events

import com.github.srain3.machinecraft.boat.LandBoat
import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Boat
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.geysermc.floodgate.api.FloodgateApi
import org.spigotmc.event.entity.EntityMountEvent
import java.util.*

/**
 * 乗車イベント
 */
object RideEvent: Listener {

    val boatList = mutableListOf<LandBoat>()
    private val uuidList = mutableListOf<UUID>()

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
        if (event.entity is Player) {
            (event.entity as Player).isFlying = false
        }

        val boatData = boatList.first()
        if (uuidList.contains(boatData.boat.uniqueId)) return
        uuidList.add(boatData.boat.uniqueId)

        // task
        object : BukkitRunnable() {
            override fun run() {
                if (boatData.boat.isDead) {
                    uuidList.remove(boatData.boat.uniqueId)
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

                    if (!boatData.jump()) {
                        boatData.blockHitCheck()
                    }
                }
                boatData.updateBar()
                boatData.updatePlayer()

                boatData.distanceUpdate()

                if (FloodgateApi.getInstance().isFloodgateId(player.uniqueId)) {
                    boatData.bedRockConvert(player)
                }

                return
            }
        }.runTaskTimer(ToolBox.pl, 1, 1)
    }

    val boatRegex = Regex(""".*_BOAT""")
    val topSpeedRegex = Regex("""TopSpeed: [0-9]+(\+*)?""")
    val powerRegex = Regex("""Power: [0-9]+(\+*)?""")
    val brakeRegex = Regex("""Brake: [0-9]+(\+*)?""")
    val slipRegex = Regex("""Momentum: [0-9]+(\+*|-*){0,300}""")
    private val rentalRegex = Regex("""Rental: [0-9]+""")

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

        if (clickLoc.block.type == Material.WATER) return

        val player = event.player

        var topSpeedInt = 50
        var powerInt = 40
        var brakeInt = 20
        var slipInt = 25
        var rentalInt: Int? = null
        meta.lore?.forEach { line ->
            if (topSpeedRegex.matches(line)) {
                // 最高速設定
                val rawStr = line.replace("TopSpeed: ","")
                topSpeedInt = rawStr.replace("+","").toInt()
                topSpeedInt += rawStr.count { it == '+' } * 10
            } else if (powerRegex.matches(line)) {
                // エンジンパワー設定(加速力)
                val rawStr = line.replace("Power: ","")
                powerInt = rawStr.replace("+","").toInt()
                powerInt += rawStr.count { it == '+' } * 2
            } else if (brakeRegex.matches(line)) {
                // ブレーキ力(減速力)
                val rawStr = line.replace("Brake: ","")
                brakeInt = rawStr.replace("+","").toInt()
                brakeInt += rawStr.count { it == '+' } * 2
            } else if (slipRegex.matches(line)) {
                // モーメント(スリップ)力
                val rawStr = line.replace("Momentum: ","")
                slipInt = rawStr.replace("+","").replace("-","").toInt()
                slipInt += rawStr.count { it == '+' } * 2
                slipInt -= rawStr.count { it == '-' } * 2
                if (slipInt < 0) {
                    slipInt = 0
                }
            } else if (rentalRegex.matches(line)) {
                // レンタカー機能
                rentalInt = line.replace("Rental: ", "").toInt()
            }
        }
        val normalBoat = !meta.hasLore()
        if (normalBoat) {
            rentalInt = 10
        }

        if (topSpeedInt > 500) {
            topSpeedInt = 500
        }
        if (powerInt > 500) {
            powerInt = 500
        }
        if (brakeInt > 500) {
            brakeInt = 500
        }
        if (slipInt >150) {
            slipInt = 150
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
                    val momentum = slipInt * 0.1F

                    val boatData = LandBoat(boat,Vector(),topSpeed,power,brake,bossBar,0F, momentum, mutableMapOf(), boat.location.toVector(), item.clone(), false, rentalInt, 0, player)
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
                        if (boatData.placePlayer.isOnline) {
                            entity.remove()
                            boatData.placePlayer.inventory.addItem(boatData.item)
                        } else {
                            entity.itemStack = boatData.item
                        }
                    }
                }
                boatList.remove(boatData)
            }
        }.runTaskLater(ToolBox.pl, 1)
    }
}