package com.example.kotlinnearby.Remote

import com.example.kotlinnearby.Model.MyPlaces
import com.example.kotlinnearby.Model.PlaceDetail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun  getNearbyPlaces(@Url url:String):Call<MyPlaces>

    @GET
    fun getDetailPlace(@Url url:String) :Call<PlaceDetail>
}