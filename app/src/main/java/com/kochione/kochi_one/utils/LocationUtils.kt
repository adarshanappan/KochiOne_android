package com.kochione.kochi_one.utils

import kotlin.math.*

/**
 * Computes great-circle distance between two coordinates using the Haversine formula.
 *
 * @return distance in kilometers.
 */
fun distanceInKm(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
): Double {
    val earthRadiusKm = 6371.0

    val dLat = Math.toRadians(endLat - startLat)
    val dLon = Math.toRadians(endLon - startLon)

    val lat1Rad = Math.toRadians(startLat)
    val lat2Rad = Math.toRadians(endLat)

    val a = sin(dLat / 2).pow(2.0) +
            sin(dLon / 2).pow(2.0) * cos(lat1Rad) * cos(lat2Rad)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadiusKm * c
}

