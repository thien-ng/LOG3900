package com.example.client_leger

class Constants {
    companion object {
        const val SERVER_URL = "https://log3000-app.herokuapp.com"
        const val MESSAGE_MAX_LENGTH = 144
        const val GALLERY_REQUEST_CODE = 1
        const val DEFAULT_CHANNEL_ID = "general"

        //ENDPOINTS
        const val LOGIN_ENDPOINT = "/account/login"
        const val REGISTER_ENDPOINT = "/account/register"
        const val CARD_ENDPOINT = "/card"
        const val LOBBY_JOIN_ENDPOINT = "/game/lobby/join"

        //char limits
        const val MAX_USERNAME_SiZE = 20
        const val MAX_PASSWORD_SiZE = 20
    }
}