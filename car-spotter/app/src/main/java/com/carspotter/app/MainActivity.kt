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
import coil.Coil
import coil.ImageLoader
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import okhttp3.OkHttpClient

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

        // Wikimedia's robot policy rejects requests without a descriptive
        // User-Agent, so give Coil's HTTP client one before any photo loads.
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient {
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            chain.proceed(
                                chain.request().newBuilder()
                                    .header(
                                        "User-Agent",
                                        "CarSpotter/1.1 (Android; +https://github.com/pftttt123/plugin)"
                                    )
                                    .build()
                            )
                        }
                        .build()
                }
                .crossfade(true)
                .build()
        )

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        store = SpottedStore(this)
        allCars = CarRepository.loadCars(this)

        progressText = findViewById(R.id.progressText)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        adapter = CarListAdapter(CarRepository.bundledImageIds(this)) { car -> onCarClicked(car) }
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

        val rows = ArrayList<Row>(visible.size + 256)
        for ((make, group) in visible.groupBy { it.make }) {
            rows.add(
                Row.Header(
                    make = make,
                    spotted = group.count { spotted.contains(it.id) },
                    total = group.size
                )
            )
            for (car in group) {
                rows.add(Row.CarRow(car, spotted.contains(car.id)))
            }
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
