package com.mitsuki.armory.timetrace

import org.json.JSONObject

class TraceMeta(
    var name: String = "",
    var thread: String = "",
    var startTime: Long = -1,
    var endTime: Long = -1,
    private var next: TraceMeta? = null
) {

    companion object {
        private val sPoolSync = Any()
        private var sPoolSize = 0
        private var sPool: TraceMeta? = null
        private const val MAX_POOL_SIZE = 5000

        fun obtain(): TraceMeta {
            synchronized(sPoolSync) {
                if (sPool != null) {
                    val m = sPool
                    sPool = m?.next
                    m?.next = null
                    sPoolSize--
                    return m ?: TraceMeta()
                }
            }
            return TraceMeta()
        }

        fun obtain(name: String, thread: String, startTime: Long): TraceMeta {
            val data = obtain()
            data.name = name
            data.thread = thread
            data.startTime = startTime
            return data
        }
    }

    override fun toString(): String {
        val text = if (startTime < 0 || endTime < 0 || endTime < startTime) {
            "time error(startTime:$startTime|endTime:$endTime)"
        } else {
            val time = endTime - startTime
            if (time > 9999) "${time / 1000000}ms" else "${time}ns"
        }
        return "$name[$thread]\t$text"
    }

    fun recycle() {
        name = ""
        thread = ""
        startTime = -1
        endTime = -1

        synchronized(sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool
                sPool = this
                sPoolSize++
            }
        }
    }

    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("thread", thread)
            put("start", startTime)
            put("end", endTime)
        }
    }


}