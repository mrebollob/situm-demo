package com.mrebollob.situmdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mrebollob.situmdemo.utils.toast
import es.situm.sdk.SitumSdk
import es.situm.sdk.error.Error
import es.situm.sdk.location.LocationListener
import es.situm.sdk.location.LocationManager
import es.situm.sdk.location.LocationRequest
import es.situm.sdk.location.LocationStatus
import es.situm.sdk.model.cartography.Building
import es.situm.sdk.model.cartography.Floor
import es.situm.sdk.model.cartography.Poi
import es.situm.sdk.model.location.Location
import es.situm.sdk.utils.Handler


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private val TAG = "MapsActivity"
    private val BUILDING_ID = "1843"
    private val GIGIGO = LatLng(40.446002, -3.627503)
    private var map: GoogleMap? = null
    private var mapImage: Bitmap? = null
    private var myLocationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        startPositioning()
    }

    override fun onPause() {
        super.onPause()
        stopPositioning()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(GIGIGO, 20f))


        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle)
        map?.setMapStyle(style)


        getBuilding()
    }

    private fun getBuilding() {
        SitumSdk.communicationManager().fetchBuildings(object : Handler<Collection<Building>> {

            override fun onSuccess(buildings: Collection<Building>) {
                Log.d(TAG, "onSuccess: Your buildings: ")
                for (building in buildings) {
                    Log.i(TAG, "onSuccess: " + building.identifier + " - " + building.name)

                    if (BUILDING_ID == building.identifier) {
                        displayFloorImage(building)
//                        displayPois(building)
                    }
                }

                if (buildings.isEmpty()) {
                    Log.e(TAG, "onSuccess: you have no buildings. Create one in the Dashboard")
                }
            }

            override fun onFailure(error: Error) {
                Log.e(TAG, "onFailure:" + error)
                toast("Error: " + error.message)
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
                        mapImage = bitmap
                        showGroundOverlay(building, floor.scale.toFloat(), bitmap)
                    }

                    override fun onFailure(error: Error) {
                        Log.e(TAG, "onFailure: fetching floor map: " + error)
                    }
                })
            }

            override fun onFailure(error: Error) {
                Log.e(TAG, "onFailure: fetching floors: " + error)
            }
        })
    }

    private fun displayPois(building: Building) {
        SitumSdk.communicationManager().fetchIndoorPOIsFromBuilding(building, object : Handler<Collection<Poi>> {
            override fun onSuccess(pois: Collection<Poi>) {
                map?.clear()
                pois.forEach {

                    val point = LatLng(it.coordinate.latitude, it.coordinate.longitude)

                    map?.addMarker(MarkerOptions()
                            .position(point))
                }
            }

            override fun onFailure(error: Error?) {
                Log.e(TAG, "onFailure: fetching floors: " + error)
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

        val locationRequest = LocationRequest.Builder()
                .buildingIdentifier(BUILDING_ID)
                .build()
        SitumSdk.locationManager().requestLocationUpdates(locationRequest, locationListener)
    }

    private fun stopPositioning() {
        SitumSdk.locationManager().removeUpdates(locationListener)
    }

    private fun getBitmapDescriptor(id: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this@MapsActivity, id)
        val size = 70
        vectorDrawable.setBounds(0, 0, size, size)
        val bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bm)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.i(TAG, "onLocationChanged() called with: location = [$location]")

            val myLocation = LatLng(location.coordinate.latitude, location.coordinate.longitude)

            myLocationMarker?.remove()
            myLocationMarker = map?.addMarker(MarkerOptions()
                    .position(myLocation)
                    .icon(getBitmapDescriptor(R.drawable.ic_my_location)))

        }

        override fun onStatusChanged(status: LocationStatus) {
            Log.i(TAG, "onStatusChanged() called with: status = [$status]")
        }

        override fun onError(error: Error) {
            Log.e(TAG, "onError() called with: error = [$error]")

            when (error.code) {
                LocationManager.Code.MISSING_LOCATION_PERMISSION -> requestLocationPermission()
                LocationManager.Code.LOCATION_DISABLED -> showLocationSettings()
            }
        }
    }

    private fun showLocationSettings() {
        toast("You must enable location")
        startActivityForResult(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
    }

    companion object Navigator {
        fun open(context: Context) {
            val intent = Intent(context, MapsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
