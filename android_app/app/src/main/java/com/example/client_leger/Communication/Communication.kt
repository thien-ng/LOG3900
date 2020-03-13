package com.example.client_leger.Communication

import io.reactivex.rxjava3.subjects.PublishSubject
import org.json.JSONObject

object Communication {

    private var connectSource: PublishSubject<JSONObject> = PublishSubject.create()
    private var chatSource: PublishSubject<JSONObject> = PublishSubject.create()
    private var channelSource: PublishSubject<String> = PublishSubject.create()
    private var drawSource: PublishSubject<JSONObject> = PublishSubject.create()

    fun getDrawListener(): PublishSubject<JSONObject>{
        return drawSource
    }

    fun updateDraw(obj: JSONObject) {
        drawSource.onNext(obj)
    }

    fun getChatMessageListener(): PublishSubject<JSONObject>{
        return chatSource
    }

    fun updateChatMessage(obj: JSONObject) {
        chatSource.onNext(obj)
    }

    fun getChannelUpdateListener(): PublishSubject<String>{
        return channelSource
    }

    fun updateChannels() {
        channelSource.onNext("newChannel")
    }

    fun getConnectionListener(): PublishSubject<JSONObject>{
        return connectSource
    }

    fun updateConnection(obj: JSONObject) {
        connectSource.onNext(obj)
    }

}