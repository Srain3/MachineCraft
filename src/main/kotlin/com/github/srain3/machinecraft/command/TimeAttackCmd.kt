package com.github.srain3.machinecraft.command

import com.github.srain3.machinecraft.timeattack.TimeData
import com.github.srain3.machinecraft.tools.ToolBox
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeAttackCmd: CommandExecutor {

    private val timeAttackMap = mutableMapOf<Player,MutableList<TimeData>>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (command.name != "timeattack") return false
        if (!sender.isOp) return false
        if (args.isNullOrEmpty()) return false
        if (args.size != 2) return false
        val circuitName = args[0]
        val sector = args[1].toIntOrNull() ?: return false
        when (sender) {
            is BlockCommandSender -> {
                sender.block.location.world?.getNearbyEntities(sender.block.location,4.0,4.0,4.0) {
                    it is Player
                }?.forEach { entity ->
                    if (entity !is Player) return@forEach
                    val dataList = timeAttackMap[entity]
                    if (dataList == null) {
                        if (sector == 0) {
                            timeAttackMap[entity] = mutableListOf(TimeData(circuitName, LocalTime.now(), 0L))
                            entity.sendMessage(ToolBox.colorMessage("[&9TimeAttack&r] $circuitName &6START!"))
                        }
                        return@forEach
                    } else if (dataList.any { it.circuit == circuitName }) {
                        val data = dataList.firstOrNull { it.circuit == circuitName } ?: return@forEach
                        val timeLong = LocalTime.now().toNanoOfDay() - data.oldTimeStamp.toNanoOfDay()
                        data.lapTime += timeLong
                        if (sector == 0) {
                            val localTime = LocalTime.ofNanoOfDay(data.lapTime)
                            entity.sendMessage(ToolBox.colorMessage("[&9$circuitName&r] Time = &a"+localTime.format(DateTimeFormatter.ISO_LOCAL_TIME).dropLast(4)))
                            data.oldTimeStamp = LocalTime.now()
                            data.lapTime = 0L
                        } else {
                            val localTime = LocalTime.ofNanoOfDay(timeLong)
                            entity.sendMessage(ToolBox.colorMessage("[&9$circuitName&r] sector${sector} = &r"+localTime.format(DateTimeFormatter.ISO_LOCAL_TIME).dropLast(4)))
                            data.oldTimeStamp = LocalTime.now()
                        }
                        timeAttackMap[entity] = dataList
                    } else {
                        if (sector == 0) {
                            dataList.add(TimeData(circuitName, LocalTime.now(), 0L))
                            entity.sendMessage(ToolBox.colorMessage("[&9TimeAttack&r] $circuitName &6START!"))
                            timeAttackMap[entity] = dataList
                        }
                    }
                }
            }
            else -> {
                return false
            }
        }
        return false
    }
}