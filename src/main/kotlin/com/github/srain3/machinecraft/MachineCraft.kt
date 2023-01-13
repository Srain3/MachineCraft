package com.github.srain3.machinecraft

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
        RideEvent.boatList.forEach { boatData ->
            boatData.bossBar.removeAll()
            Bukkit.removeBossBar(ToolBox.pluginNamespaceKey(boatData.boat.uniqueId.toString()))
        }
    }
}