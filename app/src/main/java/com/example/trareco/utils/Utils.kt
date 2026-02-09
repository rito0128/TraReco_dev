package com.example.trareco.utils

import android.annotation.SuppressLint
import java.time.LocalDateTime

//日時の情報を取得
object DateUtils {
    @SuppressLint("NewApi")
    fun formatToKey(num: Int): String {
        val date = LocalDateTime.now().minusDays(num.toLong())

        return "${date.year}_${date.monthValue}_${date.dayOfMonth}"
    }
}