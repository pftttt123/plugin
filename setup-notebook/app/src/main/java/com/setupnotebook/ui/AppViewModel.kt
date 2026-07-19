package com.setupnotebook.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.setupnotebook.data.AppDatabase
import com.setupnotebook.data.Car
import com.setupnotebook.data.ExportFile
import com.setupnotebook.data.ExportImport
import com.setupnotebook.data.LapEntry
import com.setupnotebook.data.Setup
import com.setupnotebook.data.SetupFields
import com.setupnotebook.data.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val carDao = db.carDao()
    private val trackDao = db.trackDao()
    private val setupDao = db.setupDao()
    private val lapDao = db.lapDao()

    private fun <T> Flow<List<T>>.state(): StateFlow<List<T>> =
        stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val cars: StateFlow<List<Car>> = carDao.all().state()
    val tracks: StateFlow<List<Track>> = trackDao.all().state()
    private val setups: StateFlow<List<Setup>> = setupDao.all().state()
    private val allLaps: StateFlow<List<LapEntry>> = lapDao.all().state()

    data class SetupItem(
        val setup: Setup,
        val car: Car?,
        val track: Track?,
        val bestLapMs: Long?,
    )

    val setupItems: StateFlow<List<SetupItem>> =
        combine(setups, cars, tracks, allLaps) { setupList, carList, trackList, lapList ->
            val carsById = carList.associateBy { it.id }
            val tracksById = trackList.associateBy { it.id }
            val bestBySetup = lapList.groupBy { it.setupId }
                .mapValues { (_, laps) -> laps.minOf { it.lapTimeMs } }
            setupList.map { s ->
                SetupItem(s, carsById[s.carId], tracksById[s.trackId], bestBySetup[s.id])
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---- setup list filters ----
    val query = MutableStateFlow("")
    val simFilter = MutableStateFlow<String?>(null)
    val carFilter = MutableStateFlow<Long?>(null)
    val trackFilter = MutableStateFlow<Long?>(null)

    val filteredSetups: StateFlow<List<SetupItem>> =
        combine(setupItems, query, simFilter, carFilter, trackFilter) { items, q, sim, carId, trackId ->
            items.filter { item ->
                (q.isBlank() || listOfNotNull(item.setup.name, item.car?.name, item.track?.name, item.car?.simTitle)
                    .any { it.contains(q, ignoreCase = true) }) &&
                    (sim == null || item.car?.simTitle == sim) &&
                    (carId == null || item.setup.carId == carId) &&
                    (trackId == null || item.setup.trackId == trackId)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---- cars ----
    fun addCar(name: String, simTitle: String) = viewModelScope.launch {
        carDao.insert(Car(name = name.trim(), simTitle = simTitle.trim()))
    }

    fun updateCar(car: Car) = viewModelScope.launch { carDao.update(car) }

    fun deleteCar(car: Car) = viewModelScope.launch { carDao.delete(car) }

    fun setupCountForCar(carId: Long): Int = setups.value.count { it.carId == carId }

    // ---- tracks ----
    fun addTrack(name: String) = viewModelScope.launch { trackDao.insert(Track(name = name.trim())) }

    fun updateTrack(track: Track) = viewModelScope.launch { trackDao.update(track) }

    fun deleteTrack(track: Track) = viewModelScope.launch { trackDao.delete(track) }

    fun setupCountForTrack(trackId: Long): Int = setups.value.count { it.trackId == trackId }

    // ---- setups ----
    fun setupById(id: Long): Flow<Setup?> = setupDao.byId(id)

    fun lapsFor(setupId: Long): Flow<List<LapEntry>> = lapDao.forSetup(setupId)

    fun createSetup(name: String, carId: Long, trackId: Long, onCreated: (Long) -> Unit) =
        viewModelScope.launch {
            val id = setupDao.insert(Setup(name = name.trim(), carId = carId, trackId = trackId))
            onCreated(id)
        }

    fun duplicateSetup(setup: Setup) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        setupDao.insert(setup.copy(id = 0, name = setup.name + " (copy)", createdAt = now, updatedAt = now))
    }

    fun deleteSetup(setup: Setup) = viewModelScope.launch { setupDao.delete(setup) }

    private val writeMutex = Mutex()

    private fun modifySetup(id: Long, transform: (Setup) -> Setup) = viewModelScope.launch {
        writeMutex.withLock {
            setupDao.byIdOnce(id)?.let {
                setupDao.update(transform(it).copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun renameSetup(id: Long, name: String) = modifySetup(id) { it.copy(name = name.trim()) }

    fun updateNotes(id: Long, notes: String) = modifySetup(id) { it.copy(notes = notes) }

    fun setFieldValue(id: Long, key: String, value: Double) = modifySetup(id) { setup ->
        val values = SetupFields.parseValues(setup.valuesJson).toMutableMap()
        values[key] = value
        setup.copy(valuesJson = SetupFields.encodeValues(values))
    }

    // ---- laps ----
    fun addLap(setupId: Long, lapTimeMs: Long, conditions: String, airTemp: Double?, trackTemp: Double?) =
        viewModelScope.launch {
            lapDao.insert(
                LapEntry(
                    setupId = setupId,
                    lapTimeMs = lapTimeMs,
                    conditions = conditions.trim(),
                    airTemp = airTemp,
                    trackTemp = trackTemp,
                ),
            )
        }

    fun deleteLap(lap: LapEntry) = viewModelScope.launch { lapDao.delete(lap) }

    // ---- export / import ----
    fun exportJsonFor(item: SetupItem): String {
        val laps = allLaps.value.filter { it.setupId == item.setup.id }
        return ExportImport.encode(
            ExportFile(setups = listOf(ExportImport.toExport(item.setup, item.car, item.track, laps))),
        )
    }

    /** Parses [text] and inserts all contained setups, creating cars/tracks by name as needed.
     *  Calls [onDone] with the number of imported setups, or null on parse failure. */
    fun importJson(text: String, onDone: (Int?) -> Unit) = viewModelScope.launch {
        val file = runCatching { ExportImport.decode(text) }.getOrNull()
        if (file == null || file.setups.isEmpty()) {
            onDone(null)
            return@launch
        }
        var imported = 0
        for (exp in file.setups) {
            val car = carDao.findByNameAndSim(exp.car, exp.simTitle)
                ?: Car(id = carDao.insert(Car(name = exp.car, simTitle = exp.simTitle)), name = exp.car, simTitle = exp.simTitle)
            val track = trackDao.findByName(exp.track)
                ?: Track(id = trackDao.insert(Track(name = exp.track)), name = exp.track)
            val setupId = setupDao.insert(
                Setup(
                    name = exp.name,
                    carId = car.id,
                    trackId = track.id,
                    notes = exp.notes,
                    valuesJson = SetupFields.encodeValues(exp.values),
                ),
            )
            exp.laps.forEach { lap ->
                lapDao.insert(
                    LapEntry(
                        setupId = setupId,
                        lapTimeMs = lap.lapTimeMs,
                        conditions = lap.conditions,
                        airTemp = lap.airTemp,
                        trackTemp = lap.trackTemp,
                        loggedAt = if (lap.loggedAt > 0) lap.loggedAt else System.currentTimeMillis(),
                    ),
                )
            }
            imported++
        }
        onDone(imported)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AppViewModel(this[APPLICATION_KEY] as Application) }
        }
    }
}
