package com.example.music

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase


@Entity(tableName = "offline_track")
data class OfflineTrack(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name : String,
    val image: String,
    val audioUrl : String,
    val localPath : String,
    val downloadDate: Long
)

@Dao
interface OfflineTrackDao{
    @Query("SELECT * FROM offline_track ORDER BY downloadDate DESC")
    fun getAllTracks(): List<OfflineTrack>
    @Query("SELECT *FROM offline_track WHERE audioUrl =  :audioUrl LIMIT 1")
    fun getTrackByUrl(audioUrl: String): OfflineTrack?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(track: OfflineTrack)
    @Delete
    fun deleteTrack(track: OfflineTrack)
}

@Database(entities = [OfflineTrack::class], version = 1)
abstract class OfflineTrackDatabase : RoomDatabase(){
    abstract fun offlineTrackDao(): OfflineTrackDao

    companion object{
        @Volatile
        private var INSTANCE: OfflineTrackDatabase? = null

        fun getInstance(context: Context): OfflineTrackDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineTrackDatabase::class.java,
                    "offline_track_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}