package com.github.srain3.machinecraft.tools

import com.github.srain3.machinecraft.MachineCraft
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit.getServer
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.plugin.RegisteredServiceProvider


/**
 * 細々した呼び出し頻度のある便利ボックス
 */
object ToolBox {
    /**
     * メインクラス[MachineCraft]を利用できるようにする変数
     */
    lateinit var pl: MachineCraft

    /**
     * &をMinecraftで使われている装飾用記号(§)に変換して返す
     * @param message 変換したい文字列
     * @return &を§に変換した文字列
     */
    fun colorMessage(message: String): String {
        return ChatColor.translateAlternateColorCodes('&', message)
    }

    fun pluginNamespaceKey(string: String): NamespacedKey {
        return NamespacedKey(pl, "MachineCraft-$string")
    }

    /**
     * Vault(お金)扱うやつ
     */
    var econ: Economy? = null

    fun setupEconomy(): Boolean {
        if (getServer().pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy> =
            getServer().servicesManager.getRegistration(Economy::class.java)
                ?: return false
        econ = rsp.provider
        return true
    }

    /**
     * レンタル走行距離料金(1mごと)
     */
    var distanceFee = 0.1

    fun getConfigToRentalFee() {
        distanceFee = pl.config.getDouble("Rental_distance_fee", 0.1)
    }

}