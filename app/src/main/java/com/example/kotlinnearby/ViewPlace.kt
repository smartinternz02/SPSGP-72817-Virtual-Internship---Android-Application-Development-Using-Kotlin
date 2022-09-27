package com.example.kotlinnearby

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.kotlinnearby.Common.Common
import com.example.kotlinnearby.Model.PlaceDetail
import com.example.kotlinnearby.Remote.IGoogleAPIService
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Response

class ViewPlace : AppCompatActivity() {
    internal lateinit var mService:IGoogleAPIService
    var mPlace: PlaceDetail?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)

        mService= Common.googleApiService
        // Set empty for all text view
        val place_name:TextView=findViewById(R.id.place_name)
        val place_address:TextView=findViewById(R.id.place_address)
        val place_open_hour:TextView=findViewById(R.id.open_hour)
        val btn_show_map:Button=findViewById(R.id.btn_show_map)
        val photo:ImageView=findViewById(R.id.photo)
        val rating_bar:RatingBar=findViewById(R.id.rating_bar)
        place_name.text=""
        place_address.text=""
        place_open_hour.text=""

        btn_show_map.setOnClickListener {
            val mapIntent=Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.result!!.url))
            startActivity(mapIntent)
        }

        // Load photo of place
        if(Common.currentResult !!.photos != null && Common.currentResult!!.photos!!.size > 0)
            Picasso.with( this)
                .load(getPhotoofPlace(Common.currentResult!!.photos!![0].photo_reference!!, 1000))
                .into(photo)
        // Load Rating
        if(Common.currentResult !!.rating !=null)
            rating_bar.rating=Common.currentResult!!.rating.toFloat()
        else
            rating_bar.visibility= View.GONE
        // Load open hours
        if(Common.currentResult!!.opening_hours != null)
            place_open_hour.text="Open now : "+Common.currentResult!!.opening_hours!!.open_now
        else
            place_open_hour.visibility=View.GONE

        // Use Service to fetch Address and Name
        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult !!.place_id !!))
            .enqueue(object : retrofit2.Callback<PlaceDetail>{
                override fun onFailure(call: Call<PlaceDetail>?, t: Throwable?) {
                    Toast.makeText(baseContext, ""+t!!.message,Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(call: Call<PlaceDetail>?, response: Response<PlaceDetail>?) {
                    mPlace=response!!.body ()
                    place_address.text = mPlace!!.result!!.formatted_address
                    place_name.text = mPlace!!.result!!.name
                }
    })
}
    private fun getPhotoofPlace(photoReference: String, maxWidth: Int): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?maxwidth=$maxWidth")
        url.append("&photoreference=$photoReference")
        url.append("skey=AIzaSyAdSpo1oaM1EdX9lPf5Jy79FnAHU8qdtgk")
        return url.toString()
    }
    private fun getPlaceDetailUrl(placeId: String): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?placeid=$placeId")
        url.append("skey=AIzaSyAdSpo1oaM1EdX9lPf5Jy79FnAHU8qdtgk")
        return url.toString()
    }
}
