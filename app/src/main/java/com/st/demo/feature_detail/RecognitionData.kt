package com.st.demo.feature_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object RecognitionData {
    val xValues = mutableListOf<Float>()
    val yValues = mutableListOf<Float>()
    val zValues = mutableListOf<Float>()

    private val _activity = MutableLiveData<String>("")
    val activity: LiveData<String> = _activity

    fun updateActivity(newActivity: String) {
        _activity.value = newActivity
    }
}