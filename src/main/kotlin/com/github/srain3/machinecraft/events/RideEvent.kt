package com.github.srain3.machinecraft.events

import com.github.srain3.machinecraft.boat.Control.boatSpeed
import com.github.srain3.machinecraft.boat.Control.jump
import com.github.srain3.machinecraft.boat.Control.speedAdd
import com.github.srain3.machinecraft.boat.SpeedBar.boatSpeedBar
import com.github.srain3.machinecraft.boat.SpeedBar.distanceUpdate
import com.github.srain3.machinecraft.boat.SpeedBar.updateBar
import com.github.srain3.machinecraft.boat.SpeedBar.updatePlayer
import com.github.srain3.machinecraft.events.VehicleMove.blockHitCheck
import com.github.srain3.machinecraft.events.VehicleMove.hitList
import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.spigotmc.event.entity.EntityMountEvent

/**
 * 乗車イベント
 */
object RideEvent: Listener {
    /**
     * ボートに乗るイベントキャッチ
     */
    @EventHandler
    fun getBoatRide(event: EntityMountEvent) {
        //Bukkit.getLogger().info("entity:${event.entity.type.name} mount:${event.mount.type.name}")
        val mount = event.mount
        if (mount !is Boat) return
        if (boatSpeed.contains(mount)) return
        if (mount.location.block.type == Material.WATER) return // 水の場合はキャンセル
        //Bukkit.getLogger().info("Mount is NewBoat!")
        boatSpeed[mount] = Vector(0.0,0.0,0.0)
        boatSpeedBar[mount] = Bukkit.createBossBar(ToolBox.pluginNamespaceKey(mount.uniqueId.toString()),"Boat Speed",BarColor.WHITE,BarStyle.SOLID)

        object : BukkitRunnable() {
            override fun run() {
                if (mount.isDead) {
                    boatSpeed.remove(mount)
                    boatSpeedBar[mount]?.removeAll()
                    Bukkit.removeBossBar(ToolBox.pluginNamespaceKey(mount.uniqueId.toString()))
                    boatSpeedBar.remove(mount)
                    hitList.remove(mount)
                    cancel()
                    return
                }
                //mount.world.playSound(mount.location, Sound.BLOCK_LAVA_POP, SoundCategory.AMBIENT, 0.6F, 0.515F)

                val player = mount.passengers.filterIsInstance<Player>().getOrNull(0)

                if (player == null) {
                    boatSpeed[mount] = Vector(0.0,0.0,0.0)
                    updateBar(mount)
                    updatePlayer(mount, mount.passengers.filterIsInstance<Player>())
                    return
                }

                val downLoc = mount.location.clone()
                downLoc.y -= 0.55
                val type = downLoc.block.type
                if (type == Material.ICE || type == Material.FROSTED_ICE || type == Material.BLUE_ICE || type == Material.PACKED_ICE) {
                    // 氷の上
                    jump(player, mount)
                    boatSpeed[mount] = Vector(0,0,0)
                } else {
                    // それ以外
                    speedAdd(player, mount)
                    val jumpSwitch = jump(player, mount)

                    updateBar(mount)
                    updatePlayer(mount, mount.passengers.filterIsInstance<Player>())

                    if (!jumpSwitch) {
                        blockHitCheck(mount)
                    }
                }
                distanceUpdate(player)

                return
            }
        }.runTaskTimer(ToolBox.pl, 1, 1)
    }

}