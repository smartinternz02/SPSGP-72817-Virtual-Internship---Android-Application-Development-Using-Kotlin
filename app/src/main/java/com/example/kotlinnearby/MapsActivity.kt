package com.example.kotlinnearby
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinnearby.Common.Common
import com.example.kotlinnearby.Model.MyPlaces

import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.kotlinnearby.Remote.IGoogleAPIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.annotation.RequiresApi as RequiresApi1

 class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

     private lateinit var mMap: GoogleMap

    private var latitude:Double=0.toDouble()
    private var longitude:Double=0.toDouble()

    private lateinit var mLastLocation: Location
    private var mMarker: Marker?=null

    // Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE: Int= 1000
    }

     private lateinit var mService: IGoogleAPIService

     internal lateinit var currentPlace: MyPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    val mapFragment= supportFragmentManager
        .findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

       //Init Service
        mService = Common.googleApiService

// Request runtime permission
    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        if(checkLocationPermission()){
        buildLocationRequest()
        buildLocationCallBack()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }
        else {
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )

        }
        val bottom_navigation_view : BottomNavigationView=findViewById(R.id.bottom_navigation_view)
        bottom_navigation_view.setOnItemSelectedListener { item ->
            when(item.itemId)
            {
                R.id.action_hospital -> nearByPlace("hospital")
                R.id.action_market -> nearByPlace("market")
                R.id.action_restaurant -> nearByPlace("restaurant")
                R.id.action_school -> nearByPlace("school")
            }
            true
        }
   }
     private fun nearByPlace (typePlace:String) {

         mMap.clear()

         val url = getUrl(latitude,longitude,typePlace)

         mService.getNearbyPlaces(url)
             .enqueue(object : Callback<MyPlaces> {
                 override fun onResponse(call: Call<MyPlaces>?, response: Response<MyPlaces>?){

                     currentPlace = response!!.body()!!
                     if(response.isSuccessful) {

                         for (i in 0 until response.body()!!.results!!.size) {
                             val markerOptions=MarkerOptions()
                             val googlePlace = response.body()!!.results!![i]
                             val lat = googlePlace.geometry!!.location!!.lat
                             val lng=googlePlace.geometry!!.location!!.lng
                             val placeName = googlePlace.name
                             val latLng = LatLng(lng,lat)

                             markerOptions.position(latLng)
                             markerOptions.title(placeName)
                             if (typePlace.equals("hospital"))
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital))
                             else if (typePlace.equals("market"))
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_market))
                             else if (typePlace.equals("restaurant"))
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))
                             else if (typePlace.equals("school"))
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school))
                             else
                                 markerOptions.icon(BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_BLUE))

                             markerOptions.snippet(i.toString()) // Assign index for Market

                             // Add marker to map
                             mMap.addMarker(markerOptions)
                             // Move camera
                             mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                             mMap.animateCamera(CameraUpdateFactory.zoomTo(17f))

                         }


                     }
                 }
                 override fun onFailure(call:Call<MyPlaces>?, t:Throwable?){
                     Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()
                 }
             })
     }

     private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {
         val googlePlaceUrl =
             StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
         googlePlaceUrl.append("?location=$latitude,$longitude")
         googlePlaceUrl.append("&radius=10000")
         googlePlaceUrl.append("&type=$typePlace")
         googlePlaceUrl.append("&key=AIzaSyD8ctF-hTKRnIjyqIKul1kMbG_cvoNcZyM")
         Log.d("URL_DEBUG",googlePlaceUrl.toString())

         return googlePlaceUrl.toString()
     }

     private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                mLastLocation = p0.locations[p0.locations.size-1]

                if(mMarker != null)
                {
                    mMarker!!.remove()
                }
                latitude =mLastLocation.latitude
                longitude=mLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                val markerOptions=MarkerOptions()
                    .position(latLng)
                    .title("Your Position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker =mMap.addMarker(markerOptions)

            //Move Camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17f))
            }
        }
    }
    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 10f
        }
    }

    private fun checkLocationPermission():Boolean {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
           if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
               ActivityCompat.requestPermissions(this, arrayOf(
                   android.Manifest.permission.ACCESS_FINE_LOCATION
               ),MY_PERMISSION_CODE)
           else
               ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),MY_PERMISSION_CODE)
           return false
       }
       else
           return true
    }

    //Override OnRequestPermissionResult

   @RequiresApi1(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       when(requestCode)
        {
            MY_PERMISSION_CODE-> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallBack()

                            fusedLocationProviderClient =
                                LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            )
                            mMap.isMyLocationEnabled = true
                        }

                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
       }
    }

      override fun onStop() {
         fusedLocationProviderClient.removeLocationUpdates(locationCallback)
         super.onStop()
     }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Init Google Play Services
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            }
        }
    else
         mMap.isMyLocationEnabled=true

        mMap!!.setOnMarkerClickListener { marker ->
            Common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]

            startActivity(Intent(this@MapsActivity,ViewPlace::class.java))
            true
        }
     }

}




