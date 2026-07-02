package com.carspotter.app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator

class MainActivity : AppCompatActivity() {

    private enum class Filter { ALL, SPOTTED, UNSPOTTED }

    private lateinit var store: SpottedStore
    private lateinit var adapter: CarListAdapter
    private lateinit var progressText: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var emptyView: TextView

    private var allCars: List<Car> = emptyList()
    private var query: String = ""
    private var filter: Filter = Filter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        store = SpottedStore(this)
        allCars = CarRepository.loadCars(this)

        progressText = findViewById(R.id.progressText)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        adapter = CarListAdapter { car -> onCarClicked(car) }
        val list = findViewById<RecyclerView>(R.id.carList)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<EditText>(R.id.searchInput).doAfterTextChanged { text ->
            query = text?.toString().orEmpty()
            refresh()
        }

        findViewById<ChipGroup>(R.id.filterChips).setOnCheckedStateChangeListener { _, checkedIds ->
            filter = when (checkedIds.firstOrNull()) {
                R.id.chipSpotted -> Filter.SPOTTED
                R.id.chipUnspotted -> Filter.UNSPOTTED
                else -> Filter.ALL
            }
            refresh()
        }

        refresh()
    }

    private fun onCarClicked(car: Car) {
        store.toggle(car.id)
        refresh()
    }

    private fun refresh() {
        val spotted = store.spottedIds()

        val visible = allCars.filter { car ->
            car.matches(query) && when (filter) {
                Filter.ALL -> true
                Filter.SPOTTED -> spotted.contains(car.id)
                Filter.UNSPOTTED -> !spotted.contains(car.id)
            }
        }

        val rows = ArrayList<Row>(visible.size + 48)
        var currentMake: String? = null
        for (car in visible) {
            if (car.make != currentMake) {
                currentMake = car.make
                val group = visible.filter { it.make == car.make }
                rows.add(
                    Row.Header(
                        make = car.make,
                        spotted = group.count { spotted.contains(it.id) },
                        total = group.size
                    )
                )
            }
            rows.add(Row.CarRow(car, spotted.contains(car.id)))
        }
        adapter.submitList(rows)

        emptyView.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE

        val total = allCars.size
        val done = allCars.count { spotted.contains(it.id) }
        progressText.text = getString(R.string.progress_format, done, total)
        progressBar.max = total
        progressBar.setProgressCompat(done, true)
    }
}
