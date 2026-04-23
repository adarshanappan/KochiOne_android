package com.kochione.kochi_one.transit.Metro.data

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.kochione.kochi_one.transit.Metro.MetroStation
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class ShapePoint(val latLng: LatLng, val dist: Double)
data class StopTime(val stopId: String, val arrivalTime: Int, val departureTime: Int, val distTraveled: Double)
data class TripInfo(val tripId: String, val shapeId: String, val stopTimes: List<StopTime>)
data class TrainScheduleEntry(
    val departureFromOrigin: Int,  // seconds since midnight
    val arrivalAtDest: Int,        // seconds since midnight
    val tripId: String
) {
    val durationMins: Int get() = (arrivalAtDest - departureFromOrigin) / 60
}

object KmrlOpenData {
    val stations = androidx.compose.runtime.mutableStateListOf<MetroStation>()
    val routePoints = androidx.compose.runtime.mutableStateListOf<LatLng>()
    val routePoints2 = androidx.compose.runtime.mutableStateListOf<LatLng>()
    val metroLinePoints = androidx.compose.runtime.mutableStateListOf<LatLng>()

    var trips = emptyList<TripInfo>()
    val shapePointsMap = mutableMapOf<String, List<ShapePoint>>()

    // Fare data: fareId -> price (INR)
    private val fareAttributes = mutableMapOf<String, Double>()
    // (originStopId, destStopId) -> price (INR)
    private val fareRules = mutableMapOf<Pair<String, String>, Double>()

    fun getFare(originId: String, destId: String): Double? =
        fareRules[Pair(originId, destId)]

    suspend fun load(context: Context) {
        try {
            loadStations(context)
            loadShapes(context)
            val tripToShape = loadTrips(context)
            loadStopTimes(context, tripToShape)
            loadFareAttributes(context)
            loadFareRules(context)
            loadRouteJson(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadStations(context: Context) {
        val st = mutableListOf<MetroStation>()
        withContext(Dispatchers.IO) {
            context.assets.open("metro_data/stops.txt").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 4) {
                        val stopId = parts[0]
                        val lat = parts[1].toDoubleOrNull() ?: continue
                        val lon = parts[2].toDoubleOrNull() ?: continue
                        st.add(MetroStation(stopId, parts[3], LatLng(lat, lon)))
                    }
                }
            }
        }
        withContext(Dispatchers.Main) {
            stations.clear()
            stations.addAll(st)
        }
    }

    private suspend fun loadShapes(context: Context) {
        val shapes = mutableMapOf<String, MutableList<ShapePoint>>()
        withContext(Dispatchers.IO) {
            context.assets.open("metro_data/shapes.txt").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 5) {
                        val shapeId = parts[0]
                        val lat = parts[2].toDoubleOrNull() ?: continue
                        val lon = parts[3].toDoubleOrNull() ?: continue
                        val dist = parts[4].toDoubleOrNull() ?: 0.0
                        shapes.getOrPut(shapeId) { mutableListOf() }.add(ShapePoint(LatLng(lat, lon), dist))
                    }
                }
            }
        }
        withContext(Dispatchers.Main) {
            shapePointsMap.clear()
            shapePointsMap.putAll(shapes)
            routePoints.clear()
            // Flat map all shapes since we just need to plot every path
            routePoints.addAll(shapes["R1_0"]?.map { it.latLng } ?: emptyList())
            routePoints2.clear()
            routePoints2.addAll(shapes["R1_1"]?.map { it.latLng } ?: emptyList())
        }
    }

    private suspend fun loadTrips(context: Context): Map<String, String> {
        val tripToShape = mutableMapOf<String, String>()
        withContext(Dispatchers.IO) {
            context.assets.open("metro_data/trips.txt").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 5) {
                        tripToShape[parts[2]] = parts[4]
                    }
                }
            }
        }
        return tripToShape
    }

    private fun parseTime(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size == 3) {
            return parts[0].toInt() * 3600 + parts[1].toInt() * 60 + parts[2].toInt()
        }
        return 0
    }

    private suspend fun loadStopTimes(context: Context, tripToShape: Map<String, String>) {
        val tMap = mutableMapOf<String, MutableList<StopTime>>()
        withContext(Dispatchers.IO) {
            context.assets.open("metro_data/stop_times.txt").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 7) {
                        val tripId = parts[0]
                        val stopId = parts[2]
                        val arr = parseTime(parts[3])
                        val dep = parseTime(parts[4])
                        val dist = parts[6].toDoubleOrNull() ?: 0.0
                        tMap.getOrPut(tripId) { mutableListOf() }.add(StopTime(stopId, arr, dep, dist))
                    }
                }
            }
        }
        val result = tMap.map { (tripId, stopTimes) ->
            TripInfo(tripId, tripToShape[tripId] ?: "R1_0", stopTimes.sortedBy { it.arrivalTime })
        }
        withContext(Dispatchers.Main) {
            trips = result
        }
    }

    private suspend fun loadFareAttributes(context: Context) {
        withContext(Dispatchers.IO) {
            context.assets.open("metro_data/fare_attributes.txt").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine() // skip header: fare_id,price,agency_id,...
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 2) {
                        fareAttributes[parts[0]] = parts[1].toDoubleOrNull() ?: 0.0
                    }
                }
            }
        }
    }

    private suspend fun loadFareRules(context: Context) {
        withContext(Dispatchers.IO) {
            context.assets.open("metro_data/fare_rules.txt").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine() // skip header: origin_id,destination_id,fare_id
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 3) {
                        val price = fareAttributes[parts[2]] ?: 0.0
                        fareRules[Pair(parts[0], parts[1])] = price
                    }
                }
            }
        }
    }

    private suspend fun loadRouteJson(context: Context) {
        val points = mutableListOf<LatLng>()
        withContext(Dispatchers.IO) {
            try {
                context.assets.open("metro_data/route.json").use { inputStream ->
                    val json = inputStream.bufferedReader().use { it.readText() }
                    val gson = Gson()
                    val root = gson.fromJson(json, Map::class.java)
                    val features = root["features"] as? List<*>
                    val feature = features?.getOrNull(0) as? Map<*, *>
                    val geometry = feature?.get("geometry") as? Map<*, *>
                    val coordinates = geometry?.get("coordinates") as? List<*>
                    
                    coordinates?.forEach { coord ->
                        val lngLat = coord as? List<*>
                        if (lngLat != null && lngLat.size >= 2) {
                            val lng = (lngLat[0] as? Number)?.toDouble() ?: 0.0
                            val lat = (lngLat[1] as? Number)?.toDouble() ?: 0.0
                            points.add(LatLng(lat, lng))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        withContext(Dispatchers.Main) {
            metroLinePoints.clear()
            metroLinePoints.addAll(points)
        }
    }

    /**
     * Returns all train departures for the given origin → destination stop pair,
     * sorted by departure time. Only includes trips where origin comes before destination.
     */
    fun getSchedule(originId: String, destId: String): List<TrainScheduleEntry> {
        return trips.mapNotNull { trip ->
            val originIdx = trip.stopTimes.indexOfFirst { it.stopId == originId }
            val destIdx   = trip.stopTimes.indexOfFirst { it.stopId == destId }
            if (originIdx < 0 || destIdx < 0 || originIdx >= destIdx) return@mapNotNull null
            TrainScheduleEntry(
                departureFromOrigin = trip.stopTimes[originIdx].departureTime,
                arrivalAtDest       = trip.stopTimes[destIdx].arrivalTime,
                tripId              = trip.tripId
            )
        }.sortedBy { it.departureFromOrigin }
    }

    /** Stop name by ID, falls back to the raw ID if not found. */
    fun getStopName(stopId: String): String =
        stations.firstOrNull { it.stopId == stopId }?.name ?: stopId

    /**
     * Returns all stops (stopName, departureTime) between origin and dest (inclusive)
     * for the given trip (preserving direction).
     */
    fun getTripStops(tripId: String, originId: String, destId: String): List<Pair<String, Int>> {
        val trip = trips.firstOrNull { it.tripId == tripId } ?: return emptyList()
        val originIdx = trip.stopTimes.indexOfFirst { it.stopId == originId }
        val destIdx   = trip.stopTimes.indexOfFirst { it.stopId == destId }
        if (originIdx < 0 || destIdx < 0 || originIdx > destIdx) return emptyList()
        return trip.stopTimes.subList(originIdx, destIdx + 1).map { st ->
            Pair(getStopName(st.stopId), st.departureTime)
        }
    }
}
