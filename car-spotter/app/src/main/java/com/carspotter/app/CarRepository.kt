package com.carspotter.app

import android.content.Context
import org.json.JSONArray

object CarRepository {

    @Volatile
    private var cache: List<Car>? = null

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
                        years = o.getString("years"),
                        body = o.getString("body")
                    )
                )
            }
            val sorted = cars.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it: Car -> it.make }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.model }
            )
            cache = sorted
            return sorted
        }
    }
}
