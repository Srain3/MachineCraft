package com.github.srain3.machinecraft.command

import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object MachineCraftCmd: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name != "machinecraft") return false
        if (args.isEmpty()) return false
        when (args[0]) {
            "reload" -> {
                ToolBox.pl.reloadConfig()
                ToolBox.getConfigToRentalFee()
                sender.sendMessage("[MachineCraft] config reload!")
            }
        }
        return true
    }
}