package com.carspotter.app

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

sealed class Row {
    data class Header(val make: String, val spotted: Int, val total: Int) : Row()
    data class CarRow(val car: Car, val spotted: Boolean) : Row()

    val stableKey: String
        get() = when (this) {
            is Header -> "header:$make"
            is CarRow -> "car:${car.id}"
        }
}

class CarListAdapter(
    private val bundledImageIds: Set<String>,
    private val onCarClick: (Car) -> Unit
) : ListAdapter<Row, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Row.Header -> TYPE_HEADER
        is Row.CarRow -> TYPE_CAR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderHolder(inflater.inflate(R.layout.item_header, parent, false))
        } else {
            CarHolder(
                inflater.inflate(R.layout.item_car, parent, false),
                bundledImageIds,
                onCarClick
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is Row.Header -> (holder as HeaderHolder).bind(row)
            is Row.CarRow -> (holder as CarHolder).bind(row)
        }
    }

    class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.headerName)
        private val count: TextView = view.findViewById(R.id.headerCount)

        fun bind(row: Row.Header) {
            name.text = row.make
            count.text = itemView.context.getString(
                R.string.header_count, row.spotted, row.total
            )
        }
    }

    class CarHolder(
        view: View,
        private val bundledImageIds: Set<String>,
        onCarClick: (Car) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val photo: ImageView = view.findViewById(R.id.carPhoto)
        private val model: TextView = view.findViewById(R.id.carModel)
        private val details: TextView = view.findViewById(R.id.carDetails)
        private val check: ImageView = view.findViewById(R.id.spottedCheck)
        private var current: Car? = null

        init {
            view.setOnClickListener { current?.let(onCarClick) }
        }

        fun bind(row: Row.CarRow) {
            current = row.car
            model.text = row.car.model
            details.text = if (row.car.body.isBlank()) {
                row.car.years
            } else {
                itemView.context.getString(
                    R.string.car_details, row.car.years, row.car.body
                )
            }
            val source: String? = when {
                row.car.id in bundledImageIds -> row.car.imageAssetPath
                else -> row.car.img
            }
            photo.load(source) {
                crossfade(true)
                placeholder(R.drawable.ic_car_placeholder)
                error(R.drawable.ic_car_placeholder)
                fallback(R.drawable.ic_car_placeholder)
            }
            if (row.spotted) {
                model.paintFlags = model.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                check.visibility = View.VISIBLE
                itemView.alpha = 0.55f
            } else {
                model.paintFlags = model.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                check.visibility = View.GONE
                itemView.alpha = 1f
            }
        }
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CAR = 1

        val DIFF = object : DiffUtil.ItemCallback<Row>() {
            override fun areItemsTheSame(oldItem: Row, newItem: Row): Boolean =
                oldItem.stableKey == newItem.stableKey

            override fun areContentsTheSame(oldItem: Row, newItem: Row): Boolean =
                oldItem == newItem
        }
    }
}
