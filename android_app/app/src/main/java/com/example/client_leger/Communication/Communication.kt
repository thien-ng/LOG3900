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

    private var lobbySource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getLobbyUpdateListener(): PublishSubject<JSONObject>{ return lobbySource }
    fun updateLobby(obj: JSONObject) { lobbySource.onNext(obj) }

    private var lobbyChat: PublishSubject<JSONObject> = PublishSubject.create()
    fun getLobbyChatListener(): PublishSubject<JSONObject>{ return lobbyChat }
    fun updateLobbyChat(obj: JSONObject) { lobbyChat.onNext(obj) }

    private var connectSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getConnectionListener(): PublishSubject<JSONObject>{ return connectSource }
    fun updateConnection(obj: JSONObject) { connectSource.onNext(obj) }

    private var gameStartSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getGameStartListener(): PublishSubject<JSONObject>{ return gameStartSource }
    fun updateGameStart() { gameStartSource.onNext(JSONObject()) }

    private var gameChatSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getGameChatListener(): PublishSubject<JSONObject>{ return gameChatSource }
    fun updateGameChat(obj: JSONObject) { gameChatSource.onNext(obj) }

    private var gamePointsSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getGamePointsListener(): PublishSubject<JSONObject>{ return gamePointsSource }
    fun updateGamePoints(obj: JSONObject) { gamePointsSource.onNext(obj) }

    private var guessLeftSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getGuessLeftListener(): PublishSubject<JSONObject>{ return guessLeftSource }
    fun updateGuessLeft(obj: JSONObject) { guessLeftSource.onNext(obj) }

    private var gameEndSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getEndGameListener(): PublishSubject<JSONObject>{ return gameEndSource }
    fun updateEndGame(obj: JSONObject) { gameEndSource.onNext(obj) }

    private var timerSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getTimerListener(): PublishSubject<JSONObject>{ return timerSource }
    fun updateTimer(obj: JSONObject) { timerSource.onNext(obj) }

    private var drawerUpdateSource: PublishSubject<JSONObject> = PublishSubject.create()
    fun getDrawerUpdateListener(): PublishSubject<JSONObject>{ return drawerUpdateSource }
    fun updateDrawer(obj: JSONObject) { drawerUpdateSource.onNext(obj) }
}