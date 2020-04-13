package com.example.client_leger.models

import android.text.format.DateUtils

class Stats(totalGame: Int, winRate: Double, bestScore: Int, totalPlayTime_s: Int, avgGameTime_s: Double ) {
    var totalGame = totalGame
    var winRate = winRate.toString()
    var bestScore = bestScore
    var totalPlayTime: String = totalPlayTime_s.toString()
    var avgGameTime:String = avgGameTime_s.toString()
}
