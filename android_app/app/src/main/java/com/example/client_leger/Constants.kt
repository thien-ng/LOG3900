package com.example.client_leger

class Constants {
    companion object {
        const val SERVER_URL = "https://log3000-app.herokuapp.com"
        const val MESSAGE_MAX_LENGTH = 144
        const val GALLERY_REQUEST_CODE = 1
        const val DEFAULT_CHANNEL_ID = "general"
        const val GAME_CHANNEL_ID = "Game Channel"

        //ENDPOINTS
        const val LOGIN_ENDPOINT = "/account/login"
        const val REGISTER_ENDPOINT = "/account/register"
        const val LOBBY_JOIN_ENDPOINT = "/game/lobby/join"
        const val ACTIVE_LOBBY_ENDPOINT = "/game/lobby/active"
        const val USER_INFO_ENDPOINT = "/account/user/info/"
        const val START_GAME_ENDPOINT = "/game/start/"
        const val USERS_LOBBY_ENDPOINT = "/game/lobby/users/"
        const val LEAVE_LOBBY_ENDPOINT = "/game/lobby/leave"
        //char limits
        const val MAX_USERNAME_SiZE = 20
        const val MAX_PASSWORD_SiZE = 20
    }
}