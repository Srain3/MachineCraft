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
        if (!sender.isOp) return null
        return when (args.size) {
            0,1 -> {
                mutableListOf("reload","give","giveTuning")
            }

            2 -> {
                return when (args[0]) {
                    "give" -> {
                        MachineCraftCmd.boatNameList.keys.toMutableList()
                    }
                    "giveTuning" -> {
                        mutableListOf("[Name(文字)]")
                    }
                    else -> {
                        null
                    }
                }
            }

            3 -> {
                return when (args[0]) {
                    "give" -> {
                        mutableListOf("[Name(文字)]")
                    }
                    "giveTuning" -> {
                        mutableListOf("[TopSpeed]")
                    }
                    else -> {
                        null
                    }
                }
            }
            4 -> {
                return when (args[0]) {
                    "give" -> {
                        mutableListOf("[TopSpeed]")
                    }
                    "giveTuning" -> {
                        mutableListOf("[Power]")
                    }
                    else -> {
                        null
                    }
                }
            }
            5 -> {
                return when (args[0]) {
                    "give" -> {
                        mutableListOf("[Power]")
                    }
                    "giveTuning" -> {
                        mutableListOf("[Brake]")
                    }
                    else -> {
                        null
                    }
                }
            }
            6 -> {
                return when (args[0]) {
                    "give" -> {
                        mutableListOf("[Brake]")
                    }
                    "giveTuning" -> {
                        mutableListOf("[Momentum]")
                    }
                    else -> {
                        null
                    }
                }
            }
            7 -> {
                return when (args[0]) {
                    "give" -> {
                        mutableListOf("[Momentum]")
                    }
                    else -> {
                        null
                    }
                }
            }
            8 -> {
                return when (args[0]) {
                    "give" -> {
                        mutableListOf("[Rental]")
                    }
                    else -> {
                        null
                    }
                }
            }

            else -> {
                null
            }
        }
    }
}