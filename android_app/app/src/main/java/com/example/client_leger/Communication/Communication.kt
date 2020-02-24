package com.example.client_leger.Communication

import io.reactivex.rxjava3.subjects.PublishSubject
import org.json.JSONObject

object Communication {

    private var connectSource: PublishSubject<JSONObject> = PublishSubject.create()
    private var chatSource: PublishSubject<JSONObject> = PublishSubject.create()

    fun getChatMessageListener(): PublishSubject<JSONObject>{
        return chatSource
    }

    fun updateChatMessage(obj: JSONObject) {
        chatSource.onNext(obj)
    }

    fun getConnectionListener(): PublishSubject<JSONObject>{
        return connectSource
    }

    fun updateConnection(obj: JSONObject) {
        connectSource.onNext(obj)
    }

}