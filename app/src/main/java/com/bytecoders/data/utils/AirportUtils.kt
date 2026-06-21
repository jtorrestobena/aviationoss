package com.bytecoders.data.utils

object AirportUtils {
    private val AIRPORT_COORDS = mapOf(
        // North America
        "ATL" to Pair(33.6407, -84.4277),
        "DFW" to Pair(32.8998, -97.0403),
        "DEN" to Pair(39.8561, -104.6737),
        "ORD" to Pair(41.9742, -87.9073),
        "LAX" to Pair(33.9416, -118.4085),
        "JFK" to Pair(40.6413, -73.7781),
        "LAS" to Pair(36.0840, -115.1537),
        "MCO" to Pair(28.4281, -81.3160),
        "MIA" to Pair(25.7959, -80.2870),
        "CLT" to Pair(35.2140, -80.9431),
        "SEA" to Pair(47.4502, -122.3088),
        "EWR" to Pair(40.6895, -74.1745),
        "SFO" to Pair(37.6213, -122.3790),
        "PHX" to Pair(33.4352, -112.0101),
        "IAH" to Pair(29.9902, -95.3368),
        "BOS" to Pair(42.3656, -71.0096),
        "MSP" to Pair(44.8848, -93.2223),
        "DTW" to Pair(42.2124, -83.3533),
        "PHL" to Pair(39.8729, -75.2437),
        "SLC" to Pair(40.7899, -111.9791),
        "BWI" to Pair(39.1774, -76.6684),
        "IAD" to Pair(38.9531, -77.4565),
        "TPA" to Pair(27.9755, -82.5333),
        "SAN" to Pair(32.7338, -117.1933),
        "DCA" to Pair(38.8512, -77.0402),
        "MDW" to Pair(41.7860, -87.7524),
        "FLL" to Pair(26.0742, -80.1506),
        "PDX" to Pair(45.5898, -122.5951),
        "AUS" to Pair(30.1945, -97.6699),
        "YYZ" to Pair(43.6777, -79.6248),
        "YVR" to Pair(49.1967, -123.1815),
        "MEX" to Pair(19.4363, -99.0721),

        // Europe
        "LHR" to Pair(51.4700, -0.4543),
        "CDG" to Pair(49.0097, 2.5479),
        "AMS" to Pair(52.3105, 4.7683),
        "FRA" to Pair(50.0379, 8.5622),
        "IST" to Pair(41.2752, 28.7519),
        "MAD" to Pair(40.4839, -3.5680),
        "BCN" to Pair(41.2974, 2.0833),
        "MUC" to Pair(48.3538, 11.7861),
        "FCO" to Pair(41.8003, 12.2389),
        "LGW" to Pair(51.1537, -0.1821),
        "ZRH" to Pair(47.4582, 8.5555),
        "CPH" to Pair(55.6180, 12.6508),
        "OSL" to Pair(60.1976, 11.1004),
        "ARN" to Pair(59.6519, 17.9186),
        "DUB" to Pair(53.4264, -6.2701),
        "VIE" to Pair(48.1103, 16.5697),
        "PMI" to Pair(39.5517, 2.7388),
        "MXP" to Pair(45.6300, 8.7231),
        "DUS" to Pair(51.2895, 6.7668),
        "BER" to Pair(52.3667, 13.5033),
        "LIS" to Pair(38.7756, -9.1355),
        "ATH" to Pair(37.9356, 23.9484),
        "HEL" to Pair(60.3172, 24.9633),
        "BRU" to Pair(50.9008, 4.4844),
        "MAN" to Pair(53.3588, -2.2749),
        "GVA" to Pair(46.2370, 6.1092),
        "ORY" to Pair(48.7262, 2.3652),
        "PRG" to Pair(50.1008, 14.2600),
        "WAW" to Pair(52.1657, 20.9671),

        // Asia
        "HND" to Pair(35.5494, 139.7798),
        "NRT" to Pair(35.7720, 140.3929),
        "SIN" to Pair(1.3644, 103.9915),
        "ICN" to Pair(37.4602, 126.4407),
        "BKK" to Pair(13.6899, 100.7501),
        "HKG" to Pair(22.3080, 113.9185),
        "PEK" to Pair(40.0799, 116.5971),
        "PVG" to Pair(31.1443, 121.8083),
        "CAN" to Pair(23.3924, 113.2988),
        "SZX" to Pair(22.6269, 113.8107),
        "TFU" to Pair(30.3164, 104.4442),
        "KUL" to Pair(2.7456, 101.7072),
        "MNL" to Pair(14.5086, 121.0194),
        "DEL" to Pair(28.5562, 77.1001),
        "BOM" to Pair(19.0896, 72.8656),
        "BLR" to Pair(13.1986, 77.7066),
        "CGK" to Pair(-6.1256, 106.6559),
        "TPE" to Pair(25.0797, 121.2342),
        "KIX" to Pair(34.4320, 135.2304),
        "CKG" to Pair(29.7192, 106.6417),
        "KMG" to Pair(25.1019, 102.9289),
        "XIY" to Pair(34.4471, 108.7516),
        "HGH" to Pair(30.2295, 120.4344),
        "SHA" to Pair(31.1979, 121.3363),
        "SGN" to Pair(10.8185, 106.6588),
        "CTS" to Pair(42.7752, 141.6923),
        "FUK" to Pair(33.5859, 130.4507),

        // Middle East & Africa
        "DXB" to Pair(25.2532, 55.3657),
        "DOH" to Pair(25.2611, 51.5651),
        "RUH" to Pair(24.9576, 46.6988),
        "JED" to Pair(21.6796, 39.1565),
        "AUH" to Pair(24.4330, 54.6511),
        "MCT" to Pair(23.5933, 58.2844),
        "JNB" to Pair(-26.1367, 28.2411),
        "CPT" to Pair(-33.9715, 18.6021),
        "CAI" to Pair(30.1219, 31.4056),
        "ADD" to Pair(8.9778, 38.7993),
        "NBO" to Pair(-1.3192, 36.9275),
        "LOS" to Pair(6.5774, 3.3210),
        "CMN" to Pair(33.3675, -7.5899),

        // South America
        "GRU" to Pair(-23.4318, -46.4678),
        "GIG" to Pair(-22.8100, -43.2506),
        "EZE" to Pair(-34.8222, -58.5358),
        "BOG" to Pair(4.7016, -74.1458),
        "LIM" to Pair(-12.0219, -77.1143),
        "SCL" to Pair(-33.3930, -70.7858),
        "PTY" to Pair(9.0714, -79.3835),
        "CUN" to Pair(21.0365, -86.8771),

        // Oceania
        "SYD" to Pair(-33.9461, 151.1772),
        "MEL" to Pair(-37.6690, 144.8410),
        "BNE" to Pair(-27.3842, 153.1175),
        "AKL" to Pair(-37.0081, 174.7917),
        "PER" to Pair(-31.9403, 115.9669),
        "ADL" to Pair(-34.9450, 138.5310)
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
