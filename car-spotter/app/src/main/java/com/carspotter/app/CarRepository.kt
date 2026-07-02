package com.carspotter.app

import android.content.Context
import org.json.JSONArray

object CarRepository {

    @Volatile
    private var cache: List<Car>? = null

    @Volatile
    private var bundledIdsCache: Set<String>? = null

    fun loadCars(context: Context): List<Car> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val json = context.assets.open("cars.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(json)
            val cars = ArrayList<Car>(array.length())
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                cars.add(
                    Car(
                        id = o.getString("id"),
                        make = o.getString("make"),
                        model = o.getString("model"),
                        years = o.optString("years"),
                        body = o.optString("body"),
                        img = o.optString("img").takeIf { it.isNotBlank() }
                    )
                )
            }
            val sorted = cars.sortedWith(
                compareBy({ it.make.lowercase() }, { it.model.lowercase() })
            )
            cache = sorted
            return sorted
        }
    }

    /** Ids of cars whose photo is bundled in the APK's assets. */
    fun bundledImageIds(context: Context): Set<String> {
        bundledIdsCache?.let { return it }
        synchronized(this) {
            bundledIdsCache?.let { return it }
            val ids = context.assets.list("images")
                ?.filter { it.endsWith(".jpg") }
                ?.map { it.removeSuffix(".jpg") }
                ?.toSet()
                ?: emptySet()
            bundledIdsCache = ids
            return ids
        }
    }
}
