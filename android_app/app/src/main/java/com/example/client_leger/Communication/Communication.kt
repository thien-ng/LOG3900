package com.example.client_leger.Communication

import io.reactivex.rxjava3.subjects.PublishSubject
import org.json.JSONObject

object Communication {

    private var drawSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getDrawListener(): PublishSubject<JSONObject>{ return drawSource }
    fun updateDraw(obj: JSONObject) { drawSource.onNext(obj) }

    private var chatSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getChatMessageListener(): PublishSubject<JSONObject>{ return chatSource }
    fun updateChatMessage(obj: JSONObject) { chatSource.onNext(obj) }

    private var channelSource: PublishSubject<String> = PublishSubject.create()
    fun getChannelUpdateListener(): PublishSubject<String>{ return channelSource }
    fun updateChannels(channelId: String) { channelSource.onNext(channelId) }

    private var connectSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getConnectionListener(): PublishSubject<JSONObject>{ return connectSource }
    fun updateConnection(obj: JSONObject) { connectSource.onNext(obj) }

    private var gameStartSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getGameStartListener(): PublishSubject<JSONObject>{ return gameStartSource }
    fun updateGameStart() { gameStartSource.onNext(JSONObject()) }

    private var gameEndSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getEndGameListener(): PublishSubject<JSONObject>{ return gameEndSource }
    fun updateEndGame(obj: JSONObject) { gameEndSource.onNext(obj) }

    private var timerSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getTimerListener(): PublishSubject<JSONObject>{ return gameEndSource }
    fun updateTimer(obj: JSONObject) { timerSource.onNext(obj) }
}