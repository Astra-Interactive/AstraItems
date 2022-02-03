package com.astrainteractive.empire_items.empire_items.events.blocks

import com.astrainteractive.astralibs.Logger

class MeanTimer(val toPass: Int = 30000) {
    private val lock = Any()
    private var started = System.currentTimeMillis()
    private val times: MutableList<Double> = mutableListOf()
    private fun getList() = synchronized(lock) {
        return@synchronized times.toList()
    }

    private fun _add(time: Double) = synchronized(lock) {
        times.add(time)
    }

    private fun clear() = synchronized(lock) {
        times.clear()
    }

    fun start() = synchronized(lock) {
        started = System.currentTimeMillis()
    }

    fun add(time: Double) {
        val list = getList()
        if (System.currentTimeMillis() - started > toPass) {
            if (!list.isNullOrEmpty()) {
                val sum = list.sum() / list.size
                Logger.log("Mean time is ${sum}", "MeanTime")
            }
            clear()
            start()

        } else
            _add(time)
    }

}