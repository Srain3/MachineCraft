package com.github.srain3.machinecraft

import com.github.srain3.machinecraft.events.RideEvent
import org.bukkit.plugin.java.JavaPlugin
import com.github.srain3.machinecraft.tools.ToolBox

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
    }
}