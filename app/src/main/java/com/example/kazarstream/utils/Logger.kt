package com.example.kazarstream.utils

import android.util.Log

object Logger {
    private const val TAG = "KazarStream"
    
    fun d(message: String) {
        Log.d(TAG, message)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    fun i(message: String) {
        Log.i(TAG, message)
    }
} 