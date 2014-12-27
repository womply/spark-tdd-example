package com.bantamlabs.transactions.util

import scala.math._

object Distance {

  def distanceInMiles(lat1: Double, lng1: Double, lat2: Double, lng2: Double) = {
    val earthRadius = 3958.75D
    val dLat = toRadians(lat2 - lat1)
    val dLng = toRadians(lng2 - lng1)
    val sindLat = sin(dLat / 2)
    val sindLng = sin(dLng / 2)
    val a = pow(sindLat, 2) + pow(sindLng, 2) * cos(toRadians(lat1)) * cos(toRadians(lat2))
    val c = 2 * atan2(sqrt(a), sqrt(1-a))
    earthRadius * c
  }
}

