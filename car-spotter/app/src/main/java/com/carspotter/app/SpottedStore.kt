package com.carspotter.app

import android.content.Context

/** Persists the set of spotted car ids in SharedPreferences. */
class SpottedStore(context: Context) {

    private val prefs = context.getSharedPreferences("spotted", Context.MODE_PRIVATE)

    fun spottedIds(): Set<String> =
        prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    fun isSpotted(id: String): Boolean = spottedIds().contains(id)

    fun toggle(id: String): Boolean {
        val current = HashSet(spottedIds())
        val nowSpotted = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }
        prefs.edit().putStringSet(KEY, current).apply()
        return nowSpotted
    }

    private companion object {
        const val KEY = "spotted_ids"
    }
}
