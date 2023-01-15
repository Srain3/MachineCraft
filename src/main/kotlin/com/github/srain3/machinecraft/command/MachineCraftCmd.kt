package com.github.srain3.machinecraft.command

import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object MachineCraftCmd: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name != "machinecraft") return false
        if (args.isEmpty()) return false
        if (!sender.isOp) return false
        when (args[0]) {
            "reload" -> {
                ToolBox.pl.reloadConfig()
                ToolBox.getConfigToRentalFee()
                sender.sendMessage("[MachineCraft] config reload!")
            }
            "give" -> {
                if (sender !is Player) return false
                // give MATERIAL Name TopSpeed Power Brake Momentum Rental
                if (args.size == 8) {
                    val material = boatNameList[args[1]] ?: return false
                    val name = if (args[2] == "null") {
                        null
                    } else {
                        args[2]
                    }
                    val list = listOf(
                        args[3].toIntOrNull(),
                        args[4].toIntOrNull(),
                        args[5].toIntOrNull(),
                        args[6].toIntOrNull(),
                        args[7].toIntOrNull()
                    )
                    val stringList = listOf(
                        "TopSpeed: ",
                        "Power: ",
                        "Brake: ",
                        "Momentum: ",
                        "Rental: "
                    )

                    val boatItem = ItemStack(material)
                    val boatMeta = boatItem.itemMeta?: return false
                    boatMeta.setDisplayName(name)
                    val newLore = mutableListOf<String>()
                    list.forEachIndexed { index, int ->
                        if (int != null) {
                            newLore.add(stringList[index] + "$int")
                        }
                    }
                    boatMeta.lore = newLore
                    boatItem.itemMeta = boatMeta

                    sender.inventory.addItem(boatItem)
                    sender.sendMessage("[MachineCraft] ボートを渡しました")
                }
            }

            "giveTuning" -> {
                if (sender !is Player) return false
                // giveTuning Name TopSpeed Power Brake Momentum
                if (args.size == 6) {
                    val material = Material.PAPER
                    val name = if (args[1] == "null") {
                        null
                    } else {
                        args[1]
                    }
                    val list = listOf(
                        args[2],
                        args[3],
                        args[4],
                        args[5]
                    )
                    val stringList = listOf(
                        "TopSpeed: ",
                        "Power: ",
                        "Brake: ",
                        "Momentum: "
                    )

                    val paperItem = ItemStack(material)
                    val paperMeta = paperItem.itemMeta?: return false
                    paperMeta.setDisplayName(name)
                    val newLore = mutableListOf<String>()
                    list.forEachIndexed { index, str ->
                        if (index == 3) {
                            if (str != "null") {
                                newLore.add(stringList[index]+str.filter { it == '+' || it == '-'})
                            }
                            return@forEachIndexed
                        }
                        if (str != "null") {
                            newLore.add(stringList[index]+str.filter { it == '+' })
                        }
                    }
                    paperMeta.lore = newLore
                    paperItem.itemMeta = paperMeta

                    sender.inventory.addItem(paperItem)
                    sender.sendMessage("[MachineCraft] チューニング券を渡しました")
                }
            }
        }
        return true
    }

    val boatNameList = mutableMapOf(
        Pair("oak",Material.OAK_BOAT),
        Pair("oak_chest",Material.OAK_CHEST_BOAT),
        Pair("spruce",Material.SPRUCE_BOAT),
        Pair("spruce_chest",Material.SPRUCE_CHEST_BOAT),
        Pair("birch",Material.BIRCH_BOAT),
        Pair("birch_chest",Material.BIRCH_CHEST_BOAT),
        Pair("jungle",Material.JUNGLE_BOAT),
        Pair("jungle_chest",Material.JUNGLE_CHEST_BOAT),
        Pair("dark_oak",Material.DARK_OAK_BOAT),
        Pair("dark_oak_chest",Material.DARK_OAK_CHEST_BOAT),
        Pair("acacia",Material.ACACIA_BOAT),
        Pair("acacia_chest",Material.ACACIA_CHEST_BOAT),
        Pair("mangrove",Material.MANGROVE_BOAT),
        Pair("mangrove_chest",Material.MANGROVE_CHEST_BOAT)
    )
}