package com.github.srain3.machinecraft.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object MachineCraftCmdTab: TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (command.name != "machinecraft") return null
        return when (args.size) {
            0,1 -> {
                mutableListOf("reload")
            }

            else -> {
                null
            }
        }
    }
}