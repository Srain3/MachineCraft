package com.github.srain3.machinecraft.tools

import com.github.srain3.machinecraft.MachineCraft
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey

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
}