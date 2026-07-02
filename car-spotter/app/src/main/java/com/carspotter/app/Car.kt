package com.carspotter.app

data class Car(
    val id: String,
    val make: String,
    val model: String,
    val years: String,
    val body: String,
    val img: String? = null
) {
    val imageAssetPath: String get() = "file:///android_asset/images/$id.jpg"

    fun matches(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return make.contains(q, ignoreCase = true) ||
            model.contains(q, ignoreCase = true) ||
            "$make $model".contains(q, ignoreCase = true)
    }
}
