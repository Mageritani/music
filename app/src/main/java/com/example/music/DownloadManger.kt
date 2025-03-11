package com.example.music

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DownloadManger(private val context : Context) {

    private val client = OkHttpClient()
    private val baseDirectory : File by lazy {
        File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),"offline_music").apply {
            if (!exists()){
                mkdirs()
            }
        }
    }

    private val offlineDatabase: OfflineTrackDatabase by lazy {
        OfflineTrackDatabase.getInstance(context)
    }

    fun downloadTrack(track: Track, onProgress: (Int) -> Unit, onComplete: (Boolean, String?) -> Unit){
        val trackFile = getTrackFile(track.name,track.audio)
        if (trackFile.exists()){
            Log.d("download","音樂文件已存在")
            onComplete(true, trackFile.path)
            return
        }

        val request = Request.Builder()
            .url(track.audio)
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("download","下載失敗${e.message}")
                onComplete(false,null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful){
                    Log.e("download","下載請求失敗${response.code}")
                    onComplete(false,null)
                    return
                }

                val responseBody = response.body ?: return
                val contentLength = responseBody.contentLength()
                var downloadedBytes: Long = 0

                try {
                    trackFile.parentFile?.mkdirs()

                    FileOutputStream(trackFile).use {  outputStream ->
                        responseBody.byteStream().use { inputStream ->
                            val buffer = ByteArray(4096)
                            var bytesRead: Int

                            while (inputStream.read(buffer).also { bytesRead = it } != -1){
                                outputStream.write(buffer,0,bytesRead)
                                downloadedBytes += bytesRead

                                if (contentLength > 0 ){
                                    val progress = ((downloadedBytes * 100) / contentLength).toInt()
                                    onProgress(progress)
                                }
                            }
                            outputStream.flush()
                        }
                    }

                    saveTrackToDatabase(track,trackFile.path)

                    Log.d("download","下載完成 ${trackFile.path}")
                    onComplete(true,trackFile.path)
                }catch (e : Exception){
                    Log.e("download","保存文件出錯 ${e.message}")
                    trackFile.delete()
                    onComplete(false, null)
                }
            }
            //保存音樂到資料庫
            private fun saveTrackToDatabase(track: Track, path: String) {
                val offlineTrack = OfflineTrack(
                    id = 0, //自動生成id
                    name = track.name,
                    image = track.image,
                    audioUrl = track.audio,
                    localPath = path,
                    downloadDate = System.currentTimeMillis()
                )
                offlineDatabase.offlineTrackDao().insertTrack(offlineTrack)
            }

        })
    }

    suspend fun getAllOfflineTracks(): List<OfflineTrack>{
        return withContext(Dispatchers.IO){
            offlineDatabase.offlineTrackDao().getAllTracks()
        }
    }

    fun deleteOfflineTrack(track: OfflineTrack): Boolean{
        try {
            val file = File(track.localPath)
            if (file.exists()){
                file.delete()
            }
            offlineDatabase.offlineTrackDao().deleteTrack(track)
            return true
        }catch (e :Exception){
            Log.e("download","刪除音樂失敗 ${e.message}")
            return false
        }
    }

    fun isTrackDownloaded(trackName: String, audioUrl: String): Boolean{
        return getTrackFile(trackName,audioUrl).exists()
    }
    //獲取音樂文件
    private fun getTrackFile(name: String, audio: String): File {
        val fileName = makeValFileName(name) + "_" + audio.hashCode() + ".mp3"
        return File(baseDirectory,fileName)
    }
    //將音樂名稱轉換為有效的文件名
    private fun makeValFileName(name: String): String {
        return  name.replace("[\\\\/:*?\"<>|]".toRegex(),"_")
    }

}