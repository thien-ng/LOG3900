package com.example.client_leger.Communication

import io.reactivex.rxjava3.subjects.PublishSubject
import org.json.JSONObject

object Communication {

    private var source: PublishSubject<JSONObject> = PublishSubject.create()

    fun getChatMessageListener(): PublishSubject<JSONObject>{
        return source
    }

    fun updateChatMessage(obj: JSONObject) {
        source.onNext(obj)
    }

}