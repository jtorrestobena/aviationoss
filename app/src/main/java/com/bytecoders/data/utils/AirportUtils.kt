package com.bytecoders.data.utils

object AirportUtils {
    private val AIRPORT_COORDS = mapOf(
        "SFO" to Pair(37.6213, -122.3790),
        "DFW" to Pair(32.8998, -97.0403),
        "LHR" to Pair(51.4700, -0.4543),
        "IAD" to Pair(38.9531, -77.4565),
        "FRA" to Pair(50.0379, 8.5622),
        "JFK" to Pair(40.6413, -73.7781),
        "HND" to Pair(35.5494, 139.7798),
        "LAX" to Pair(33.9416, -118.4085),
        "ORD" to Pair(41.9742, -87.9073),
        "CDG" to Pair(49.0097, 2.5479),
        "AMS" to Pair(52.3105, 4.7683),
        "SIN" to Pair(1.3644, 103.9915),
        "DXB" to Pair(25.2532, 55.3657),
        "HKG" to Pair(22.3080, 113.9185),
        "SYD" to Pair(-33.9461, 151.1772),
        "ATL" to Pair(33.6407, -84.4277),
        "DEN" to Pair(39.8561, -104.6737),
        "PEK" to Pair(40.0799, 116.5971),
        "PVG" to Pair(31.1443, 121.8083),
        "CAN" to Pair(23.3924, 113.2988),
        "IST" to Pair(41.2752, 28.7519),
        "DEL" to Pair(28.5562, 77.1001),
        "BOM" to Pair(19.0896, 72.8656),
        "NRT" to Pair(35.7720, 140.3929),
        "KIX" to Pair(34.4320, 135.2304),
        "ICN" to Pair(37.4602, 126.4407),
        "MAD" to Pair(40.4839, -3.5680),
        "FCO" to Pair(41.8003, 12.2389),
        "MUC" to Pair(48.3538, 11.7861),
        "BCN" to Pair(41.2974, 2.0833),
        "YVR" to Pair(49.1967, -123.1815),
        "YYZ" to Pair(43.6777, -79.6248),
        "MEL" to Pair(-37.6690, 144.8410),
        "MIA" to Pair(25.7959, -80.2870),
        "SEA" to Pair(47.4502, -122.3088),
        "BOS" to Pair(42.3656, -71.0096),
        "EWR" to Pair(40.6895, -74.1745),
        "CLT" to Pair(35.2140, -80.9431),
        "PHX" to Pair(33.4352, -112.0101),
        "IAH" to Pair(29.9902, -95.3368),
        "MCO" to Pair(28.4281, -81.3160),
        "EZE" to Pair(-34.8222, -58.5358),
        "GRU" to Pair(-23.4318, -46.4678),
        "CPH" to Pair(55.6180, 12.6508),
        "ARN" to Pair(59.6519, 17.9186),
        "HEL" to Pair(60.3172, 24.9633),
        "OSL" to Pair(60.1976, 11.1004),
        "LIS" to Pair(38.7756, -9.1355),
        "ATH" to Pair(37.9356, 23.9484),
        "DOH" to Pair(25.2611, 51.5651),
        "RUH" to Pair(24.9576, 46.6988),
        "JED" to Pair(21.6796, 39.1565),
        "TPE" to Pair(25.0797, 121.2342),
        "BKK" to Pair(13.6899, 100.7501),
        "KUL" to Pair(2.7456, 101.7072),
        "MNL" to Pair(14.5086, 121.0194),
        "CGK" to Pair(-6.1256, 106.6559),
        "BNE" to Pair(-27.3842, 153.1175),
        "AKL" to Pair(-37.0081, 174.7917),
        "CPT" to Pair(-33.9715, 18.6021),
        "JNB" to Pair(-26.1367, 28.2411),
        "MEX" to Pair(19.4363, -99.0721),
        "DUB" to Pair(53.4264, -6.2701),
        "MAN" to Pair(53.3588, -2.2749),
        "BRU" to Pair(50.9008, 4.4844),
        "ZRH" to Pair(47.4582, 8.5555),
        "VIE" to Pair(48.1103, 16.5697),
        "GVA" to Pair(46.2370, 6.1092)
    )

    /**
     * Returns a Pair(Latitude, Longitude) for a given airport IATA code.
     * Includes a large set of common international hubs and a fallback algorithmic hash.
     */
    fun getAirportCoords(iata: String?): Pair<Double, Double> {
        val upper = iata?.uppercase()?.trim() ?: ""
        
        return AIRPORT_COORDS[upper] ?: run {
            val code = upper.take(3)
            val baseLat = 20.0 + (code.getOrNull(0)?.code?.rem(40) ?: 0) - 20.0
            val baseLng = (code.getOrNull(1)?.code?.rem(180) ?: 0) * if ((code.getOrNull(2)?.code?.rem(2) ?: 0) == 0) 1.0 else -1.0
            Pair(baseLat, baseLng)
        }
    }
}
