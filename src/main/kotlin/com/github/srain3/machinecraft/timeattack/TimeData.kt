package com.github.srain3.machinecraft.timeattack

import java.time.LocalTime

data class TimeData(
    val circuit: String,
    var oldTimeStamp: LocalTime,
    var lapTime: Long
)
