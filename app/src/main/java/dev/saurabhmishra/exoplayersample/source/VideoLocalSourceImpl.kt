package dev.saurabhmishra.exoplayersample.source

import android.content.SharedPreferences
import dev.saurabhmishra.data.sources.VideoLocalSource
import dev.saurabhmishra.domain.models.VideoData
import dev.saurabhmishra.exoplayersample.database.ExoplayerSampleDB
import dev.saurabhmishra.exoplayersample.database.mappers.toEntity
import dev.saurabhmishra.exoplayersample.database.mappers.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VideoLocalSourceImpl(
    private val db: ExoplayerSampleDB,
    private val sharedPreferences: SharedPreferences
): VideoLocalSource {
    override suspend fun saveVideos(videoData: List<VideoData>) {
        val entities = videoData.map { data ->
            data.toEntity()
        }
        db.videoDao().insertAll(entities)
    }

    override suspend fun getAllVideos(): List<VideoData> {
        return db.videoDao().getAllVideos().map { entity ->
            entity.toModel()
        }
    }

    override fun getAllVideosFlow(): Flow<List<VideoData>> {
        return db.videoDao().getAllVideosFlow()
            .map { entities ->
                entities.map { entity ->
                    entity.toModel()
                }
            }
    }

    override fun getVideoSuggestions(currentVideoData: VideoData): Flow<List<VideoData>> {
        return db.videoDao().getVideoSuggestions(currentVideoData.videoId)
            .map { entities ->
                entities.map { entity ->
                    entity.toModel()
                }
            }
    }

    override suspend fun deleteLocalVideos() {
        db.videoDao().deleteAllVideos()
    }

    override suspend fun getCurrentSelectedVideo(): VideoData? {
        val currentSelectedVideoId = sharedPreferences.getLong(CURRENT_SELECTED_VIDEO_ID, -1L)

        return if (currentSelectedVideoId < 0) {
            null
        } else {
            db.videoDao().getVideoForId(currentSelectedVideoId)?.toModel()
        }
    }

    override suspend fun setCurrentSelectedVideo(videoData: VideoData) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putLong(CURRENT_SELECTED_VIDEO_ID, videoData.videoId).commit()
        }
    }

    companion object {
        private const val CURRENT_SELECTED_VIDEO_ID = "CURRENT_SELECTED_VIDEO_ID"
    }
}