package com.example.kotlinnearby.Common
import com.example.kotlinnearby.Model.Results
import com.example.kotlinnearby.Remote.RetrofitClient
import com.example.kotlinnearby.Remote.IGoogleAPIService
object Common{
    private val GOOGLE_API_URL="https://maps.googleapis.com"

    var currentResult:Results?=null
    val googleApiService:IGoogleAPIService
        get()=RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)

}