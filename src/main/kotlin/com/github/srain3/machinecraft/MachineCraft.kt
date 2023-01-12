package com.github.srain3.machinecraft

import com.github.srain3.machinecraft.boat.Control.boatSpeed
import com.github.srain3.machinecraft.boat.SpeedBar.boatSpeedBar
import com.github.srain3.machinecraft.events.RideEvent
import com.github.srain3.machinecraft.events.VehicleMove
import org.bukkit.plugin.java.JavaPlugin
import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.Bukkit

/**
 * メインクラス
 */
class MachineCraft: JavaPlugin() {
    /**
     * プラグインが有効化する時に呼ばれる所
     */
    override fun onEnable() {
        ToolBox.pl = this

        server.pluginManager.registerEvents(RideEvent, this)
        server.pluginManager.registerEvents(VehicleMove, this)
    }

    override fun onDisable() {
        boatSpeed.keys.forEach { boat ->
            boatSpeedBar.values.forEach { bar ->
                bar.removeAll()
            }
            Bukkit.removeBossBar(ToolBox.pluginNamespaceKey(boat.uniqueId.toString()))
        }
    }
}