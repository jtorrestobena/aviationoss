package com.bytecoders.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.bytecoders.R
import com.bytecoders.data.local.CachedFlightEntity
import com.bytecoders.data.utils.AirportUtils

@Composable
fun FlightRouteMap(
    flight: CachedFlightEntity,
    modifier: Modifier = Modifier
) {
    val depIata = flight.departureIata ?: stringResource(R.string.n_a)
    val arrIata = flight.arrivalIata ?: stringResource(R.string.n_a)
    val depCoords = AirportUtils.getAirportCoords(depIata)
    val arrCoords = AirportUtils.getAirportCoords(arrIata)

    val showLive = flight.liveLatitude != null && flight.liveLongitude != null
    val liveLat = flight.liveLatitude ?: 0.0
    val liveLng = flight.liveLongitude ?: 0.0
    val flightIataStr = flight.flightIata ?: flight.flightNumber ?: "Flight"

    val htmlContent = remember(flight) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <style>
                html, body {
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #12131a;
                }
                #map {
                    width: 100%;
                    height: 100%;
                    background-color: #12131a;
                }
                .leaflet-container {
                    background-color: #12131a !important;
                }
                .custom-tooltip {
                    background: #1e202c !important;
                    border: 1px solid #4a5470 !important;
                    color: #ffffff !important;
                    font-family: 'Roboto', sans-serif;
                    font-size: 10px;
                    font-weight: bold;
                    border-radius: 6px;
                    padding: 2px 6px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.3);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                });

                L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19
                }).addTo(map);

                var depLatLng = [${depCoords.first}, ${depCoords.second}];
                var arrLatLng = [${arrCoords.first}, ${arrCoords.second}];

                L.circleMarker(depLatLng, {
                    radius: 7,
                    fillColor: '#3b82f6',
                    color: '#ffffff',
                    weight: 2,
                    fillOpacity: 0.95
                }).addTo(map).bindTooltip('$depIata', { permanent: true, direction: 'top', className: 'custom-tooltip' });

                L.circleMarker(arrLatLng, {
                    radius: 7,
                    fillColor: '#10b981',
                    color: '#ffffff',
                    weight: 2,
                    fillOpacity: 0.95
                }).addTo(map).bindTooltip('$arrIata', { permanent: true, direction: 'bottom', className: 'custom-tooltip' });

                var path = L.polyline([depLatLng, arrLatLng], {
                    color: '#6366f1',
                    weight: 3,
                    dashArray: '4, 6',
                    opacity: 0.75
                }).addTo(map);

                var bounds = L.latLngBounds([depLatLng, arrLatLng]);

                if ($showLive) {
                    var liveLatLng = [$liveLat, $liveLng];
                    L.circleMarker(liveLatLng, {
                        radius: 8,
                        fillColor: '#f97316',
                        color: '#ffffff',
                        weight: 2,
                        fillOpacity: 1.0
                    }).addTo(map).bindTooltip('$flightIataStr ✈', { permanent: true, direction: 'right', className: 'custom-tooltip' });
                    
                    bounds.extend(liveLatLng);
                }

                map.fitBounds(bounds, { padding: [35, 35] });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .testTag("flight_route_map")
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
