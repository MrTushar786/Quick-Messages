package com.example.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat

data class RecentMedia(
    val uri: Uri,
    val isVideo: Boolean,
    val mimeType: String,
    val dateAdded: Long
)

object GalleryHelper {

    /**
     * Returns the required storage/media permissions list for the current Android version.
     * On Android 13+ (API 33+), requests both images and video permissions.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Checks if all required media/storage permissions are granted.
     */
    fun hasPermission(context: Context): Boolean {
        return getRequiredPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Finds the single newest media item (whether photo or video) by comparing
     * the latest entries in MediaStore.Images and MediaStore.Video by DATE_ADDED.
     */
    fun getLatestMedia(context: Context): RecentMedia? {
        if (!hasPermission(context)) {
            return null
        }

        val latestImage = queryLatestImage(context)
        val latestVideo = queryLatestVideo(context)

        return when {
            latestImage == null && latestVideo == null -> null
            latestImage == null -> latestVideo
            latestVideo == null -> latestImage
            latestVideo.dateAdded > latestImage.dateAdded -> latestVideo
            else -> latestImage
        }
    }

    private fun queryLatestImage(context: Context): RecentMedia? {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        return try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                    val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)) ?: "image/*"
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    RecentMedia(
                        uri = uri,
                        isVideo = false,
                        mimeType = mimeType,
                        dateAdded = dateAdded
                    )
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun queryLatestVideo(context: Context): RecentMedia? {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.MIME_TYPE
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        return try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    val dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
                    val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)) ?: "video/*"
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    RecentMedia(
                        uri = uri,
                        isVideo = true,
                        mimeType = mimeType,
                        dateAdded = dateAdded
                    )
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
