package com.example.music

import android.util.Log
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

data class Track(
    val name: String,
    val image: String,
    val audio: String
)
data class JamendoResponse(
    val  results : List<Track>
)

fun searchMusic(query: String, onResult: (List<Track>) -> Unit){
    val clientId = "d6f19131"
    val url = "https://api.jamendo.com/v3.0/tracks/?client_id=$clientId&format=json&search=$query&limit=100"

    val request = Request.Builder()
        .url(url)
        .build()
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("api","請求失敗${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.let { responseBody ->
                val json = responseBody.string()
                val gson = Gson()
                val musicList = gson.fromJson(json,JamendoResponse::class.java)
                onResult(musicList.results)
            }
        }

    })
}