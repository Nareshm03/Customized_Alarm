package com.example.teacherscheduler.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object CacheManager {
    
    private const val CACHE_DIR = "image_cache"
    
    fun cacheImage(context: Context, key: String, bitmap: Bitmap) {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        if (!cacheDir.exists()) cacheDir.mkdirs()
        
        val file = File(cacheDir, key)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
    
    fun getCachedImage(context: Context, key: String): Bitmap? {
        val file = File(File(context.cacheDir, CACHE_DIR), key)
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }
    
    fun clearCache(context: Context) {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        cacheDir.deleteRecursively()
    }
}
