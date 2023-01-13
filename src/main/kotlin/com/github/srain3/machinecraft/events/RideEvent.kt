package com.github.srain3.machinecraft.events

import com.github.srain3.machinecraft.boat.LandBoat
import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Boat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
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
        if (!boatList.none { it.boat.uniqueId == mount.uniqueId }) return
        if (mount.location.block.type == Material.WATER) return // 水の場合はキャンセル
        //Bukkit.getLogger().info("Mount is NewBoat!")
        val bossBar = Bukkit.createBossBar(ToolBox.pluginNamespaceKey(mount.uniqueId.toString()),"Boat Speed",BarColor.WHITE,BarStyle.SOLID)
        val boatData = LandBoat(mount,Vector(),1.5,bossBar,0F, mutableMapOf(), mount.location.toVector())
        boatList.add(boatData)

        object : BukkitRunnable() {
            override fun run() {
                if (boatData.boat.isDead) {
                    boatData.bossBar.removeAll()
                    Bukkit.removeBossBar(ToolBox.pluginNamespaceKey(boatData.boat.uniqueId.toString()))
                    boatList.remove(boatData)
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

}