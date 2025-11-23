package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executor

// Helper class to manage files in MediaStore
class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {

    // Generate FileInfo for photo
    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"

        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )

        return FileInfo(
            uri = fileHelper.getUriFromFile(file),
            file = file,
            name = name,
            relativePath = fileHelper.getPicturesFolder(),
            mimeType = "image/jpeg"
        )
    }

    // Generate FileInfo for video
    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"

        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )

        return FileInfo(
            uri = fileHelper.getUriFromFile(file),
            file = file,
            name = name,
            relativePath = fileHelper.getVideosFolder(),
            mimeType = "video/mp4"
        )
    }

    // Insert image to MediaStore
    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo = it,
                contentUri = mediaContentHelper.getImageContentUri(),
                contentValues = mediaContentHelper.generateImageContentValues(it)
            )
        }
    }

    // Insert video to MediaStore
    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo = it,
                contentUri = mediaContentHelper.getVideoContentUri(),
                contentValues = mediaContentHelper.generateVideoContentValues(it)
            )
        }
    }

    // Insert file into MediaStore with I/O copy
    private fun insertToStore(fileInfo: FileInfo, contentUri: Uri, contentValues: ContentValues) {
        executor.execute {
            val insertedUri = contentResolver.insert(contentUri, contentValues)
            insertedUri?.let {
                val inputStream = contentResolver.openInputStream(fileInfo.uri)
                val outputStream = contentResolver.openOutputStream(insertedUri)
                IOUtils.copy(inputStream, outputStream)
            }
        }
    }
}
