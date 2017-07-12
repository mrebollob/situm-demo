package com.mrebollob.situmdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mrebollob.situmdemo.utils.*
import es.situm.sdk.SitumSdk
import es.situm.sdk.directions.DirectionsRequest
import es.situm.sdk.error.Error
import es.situm.sdk.location.LocationListener
import es.situm.sdk.location.LocationManager
import es.situm.sdk.location.LocationRequest
import es.situm.sdk.location.LocationStatus
import es.situm.sdk.model.cartography.Building
import es.situm.sdk.model.cartography.Floor
import es.situm.sdk.model.cartography.Poi
import es.situm.sdk.model.cartography.Point
import es.situm.sdk.model.directions.Route
import es.situm.sdk.model.location.Location
import es.situm.sdk.utils.Handler


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "MapsActivity"
    private val BUILDING_ID = "1843"
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val titleTextView: TextView by bindView(R.id.title)
    val descriptionTextView: TextView by bindView(R.id.description)
    val loadingView: ProgressBar by bindView(R.id.loading_view)

    private val GIGIGO = LatLng(40.446002, -3.627503)
    private var map: GoogleMap? = null
    private var myLocationMarker: Marker? = null
    private var currentLocation: Location? = null
    private var poiMarkers: MutableList<Marker> = ArrayList()
    private var route: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        initToolbar()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener({ onBackPressed() })
    }

    override fun onResume() {
        super.onResume()
//        startPositioning()
    }

    override fun onPause() {
        super.onPause()
        stopPositioning()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(GIGIGO, 20f))
        map?.setOnMarkerClickListener {
            if (it.tag is Poi && loadingView.isNotVisible()) {
                val poi = it.tag as Poi
                titleTextView.text = poi.name
                navigateToPoi(poi.position)
                hidePois()
                it.isVisible = true
            }
            false
        }

        map?.setOnMapClickListener {
            titleTextView.text = ""
            route?.remove()
            showPois()
        }

        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle)
        map?.setMapStyle(style)
        map?.uiSettings?.isMapToolbarEnabled = false
        map?.uiSettings?.isTiltGesturesEnabled = false

        getBuilding()
    }

    private fun getBuilding() {
        SitumSdk.communicationManager().fetchBuildings(object : Handler<Collection<Building>> {

            override fun onSuccess(buildings: Collection<Building>) {
                Log.d(TAG, "onSuccess: Your buildings: ")
                for (building in buildings) {
                    Log.i(TAG, "onSuccess: " + building.identifier + " - " + building.name)

                    descriptionTextView.text = building.name

                    if (BUILDING_ID == building.identifier) {
                        displayFloorImage(building)
                        displayPois(building)
                        startPositioning()
                    }
                }

                if (buildings.isEmpty()) {
                    toast("You have no buildings. Create one in the Dashboard")
                    Log.e(TAG, "onSuccess: you have no buildings. Create one in the Dashboard")
                }
            }

            override fun onFailure(error: Error) {
                Log.e(TAG, "onFailure:" + error)
                showError(error.message)
            }
        })
    }

    private fun displayFloorImage(building: Building) {
        SitumSdk.communicationManager().fetchFloorsFromBuilding(building, object : Handler<Collection<Floor>> {
            override fun onSuccess(floors: Collection<Floor>) {
                Log.i(TAG, "onSuccess: received levels: " + floors.size)
                val floor = ArrayList(floors)[0]
                SitumSdk.communicationManager().fetchMapFromFloor(floor, object : Handler<Bitmap> {
                    override fun onSuccess(bitmap: Bitmap) {
                        Log.i(TAG, "Image loaded")
                        showGroundOverlay(building, floor.scale.toFloat(), bitmap)
                    }

                    override fun onFailure(error: Error) {
                        Log.e(TAG, "onFailure: fetching floor map: " + error)
                        showError(error.message)
                    }
                })
            }

            override fun onFailure(error: Error) {
                Log.e(TAG, "onFailure: fetching floors: " + error)
                showError(error.message)
            }
        })
    }

    private fun displayPois(building: Building) {
        SitumSdk.communicationManager().fetchIndoorPOIsFromBuilding(building, object : Handler<Collection<Poi>> {
            override fun onSuccess(pois: Collection<Poi>) {
                poiMarkers = ArrayList()
                for (poi in pois) {
                    map?.let {
                        val marker = it.addMarker(MarkerOptions()
                                .position(poi.toLatLng())
                                .icon(poi.getIcon(this@MapsActivity)))

                        marker.tag = poi
                        poiMarkers.add(marker)
                    }
                }
            }

            override fun onFailure(error: Error) {
                Log.e(TAG, "onFailure: fetching floors: " + error)
                showError(error.message)
            }
        })
    }

    fun showGroundOverlay(building: Building, scale: Float, mapImage: Bitmap) {

        val center = LatLng(building.center.latitude, building.center.longitude)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 20f))

        map?.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(mapImage))
                .bearing(building.rotation.degrees().toFloat())
                .position(center, mapImage.width / scale, mapImage.height / scale))
    }

    private fun startPositioning() {

        loadingView.visible()
        val locationRequest = LocationRequest.Builder()
                .buildingIdentifier(BUILDING_ID)
                .build()
        SitumSdk.locationManager().requestLocationUpdates(locationRequest, locationListener)
    }

    private fun stopPositioning() {
        SitumSdk.locationManager().removeUpdates(locationListener)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.i(TAG, "onLocationChanged() called with: location = [$location]")
            loadingView.gone()

            currentLocation = location

            myLocationMarker?.remove()
            myLocationMarker = map?.addMarker(MarkerOptions()
                    .position(location.toLatLng())
                    .icon(getBitmapDescriptor(R.drawable.ic_my_location)))

        }

        override fun onStatusChanged(status: LocationStatus) {
            Log.i(TAG, "onStatusChanged() called with: status = [$status]")
        }

        override fun onError(error: Error) {
            loadingView.gone()
            Log.e(TAG, "onError() called with: error = [$error]")
            showError(error.message)

            when (error.code) {
                LocationManager.Code.MISSING_LOCATION_PERMISSION -> requestLocationPermission()
                LocationManager.Code.LOCATION_DISABLED -> showLocationSettings()
            }
        }
    }

    private fun navigateToPoi(point: Point) {
        loadingView.visible()

        if (currentLocation != null) {
            SitumSdk.directionsManager().requestDirections(DirectionsRequest.Builder()
                    .from(currentLocation as Location)
                    .isAccessible(false)
                    .to(point)
                    .build(),
                    object : Handler<Route> {
                        override fun onSuccess(route: Route) {
                            loadingView.gone()
                            drawRoute(route.points)
                        }

                        override fun onFailure(error: Error) {
                            loadingView.gone()
                            Log.e(TAG, "onFailure: request directions: " + error)
                            showError(error.message)
                        }

                    })
        }
    }

    private fun drawRoute(points: List<Point>) {

        if (map == null) {
            return
        }

        if (points.size < 2) {
            return
        }

        val options = PolylineOptions()
        options.color(Color.parseColor("#CC0000FF"))
        options.width(5f)
        options.visible(true)

        for (point in points) {
            options.add(point.toLatLng())
        }

        route = map?.addPolyline(options)
    }

    private fun hidePois() {
        for (poiMarker in poiMarkers) {
            poiMarker.isVisible = false
        }
    }

    private fun showPois() {
        for (poiMarker in poiMarkers) {
            poiMarker.isVisible = true
        }
    }

    private fun showLocationSettings() {
        toast("You must enable location")
        startActivityForResult(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
    }

    private fun showError(error: String) {
        toast(error)
    }

    companion object Navigator {
        fun open(context: Context) {
            val intent = Intent(context, MapsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
