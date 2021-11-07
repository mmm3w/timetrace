package com.mitsuki.armory.timetrace

import android.os.Trace
import android.util.Log
import org.json.JSONArray

object TimeTracer {

    var logLevel: LogLevel = LogLevel.NONE
    var isEnable = true
        set(value) {
            if (value != field) {
                field = value
                synchronized(mDataSync) {
                    mCacheData.clear()
                    mCollData.clear()
                }
            }
        }

    private val mDataSync = Any()
    private val mCacheData by lazy { hashMapOf<String, TraceMeta>() }
    private val mCollData: MutableList<TraceMeta> by lazy { arrayListOf() }

    fun start(name: String) {
        Trace.beginSection(name)
        if (isEnable) {
            synchronized(mDataSync) {
                mCacheData[name] =
                    TraceMeta.obtain(name, Thread.currentThread().name, System.nanoTime())
            }
        }
    }

    fun end(name: String) {
        Trace.endSection()
        if (isEnable) {
            synchronized(mDataSync) {
                mCacheData.remove(name)?.apply {
                    this.endTime = System.nanoTime()
                    log(this.toString())
                    mCollData.add(this)
                }
            }
        }
    }

    fun obtainStackedData(): JSONArray {
        synchronized(mDataSync) {
            val jsonArray = JSONArray()
            val mIterator = mCollData.iterator()
            while (mIterator.hasNext()) {
                val next = mIterator.next()
                jsonArray.put(next.toJSONObject())
                next.recycle()
                mIterator.remove()
            }
            return jsonArray
        }
    }

    private fun log(content: String) {
        when (logLevel) {
            LogLevel.DEBUG -> Log.d("TimeTracer", content)
            LogLevel.INFO -> Log.i("TimeTracer", content)
            else -> {
            }
        }
    }

    enum class LogLevel {
        NONE, INFO, DEBUG
    }
}