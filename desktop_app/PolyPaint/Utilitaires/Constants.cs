using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Utilitaires
{
    class Constants
    {
        //SERVER https://log3000-app.herokuapp.com http://localhost:3000
        public const string SERVER_PATH             = "https://log3000-app.herokuapp.com";
        public const string ACCOUNT_PATH            = "/account";
        public const string LOGIN_PATH              = ACCOUNT_PATH + "/login";
        public const string REGISTER_PATH           = ACCOUNT_PATH + "/register";
        public const string CHAT_MESSAGES_PATH      = "/chat/messages/";
        public const string LOBBY_MESSAGES_PATH      = "/game/lobby/messages/";
        public const string GAME_MESSAGES_PATH      = "/game/arena/messages/";
        public const string CHANNELS_PATH           = "/chat/channels";
        public const string USER_INFO_PATH          = ACCOUNT_PATH + "/user/info/";
        public const string GAME_PATH               = "/game/lobby";
        public const string USERS_LOBBY_PATH        = GAME_PATH + "/users/";
        public const string START_GAME_PATH         = "/game/start/";
        public const string GAMECARDS_PATH          = "/card";
        public const string CREATOR_PATH            = "/creator";
        public const string GAMECREATOR_PATH        = CREATOR_PATH + "/game/new";
        public const string SUGGESTION_PATH         = CREATOR_PATH + "/game/suggestion";
        public const string GAME_JOIN_PATH          = "/game/lobby/join";
        public const string GAME_LEAVE_PATH         = "/game/lobby/leave";
        public const string GET_ACTIVE_LOBBY_PATH   = "/game/lobby/active";
        public const string SUB_CHANNELS_PATH       = CHANNELS_PATH + "/sub";
        public const string NOT_SUB_CHANNELS_PATH   = CHANNELS_PATH + "/notsub";
        public const string JOIN_CHANNEL_PATH       = CHANNELS_PATH + "/join";
        public const string LEAVE_CHANNEL_PATH      = CHANNELS_PATH + "/leave";
        public const string SEARCH_CHANNEL_PATH     = CHANNELS_PATH + "/search";
        public const string CREATE_CHANNEL_PATH     = CHANNELS_PATH + "/search";
        public const int    SUCCESS_CODE            = 200;
        public const int    PWD_MIN_LENGTH          = 1; //TODO
        public const int    USR_MIN_LENGTH          = 1; //TODO

        public const string DEFAULT_CHANNEL         = "general";
        public const string GAME_CHANNEL            = "Game Channel";
        public const string LOBBY_CHANNEL           = "LOBBY: ";

        public const string SENDER_SERVER           = "server";
        public const string SENDER_ME               = "me";

        public const string MODE_FFA                = "FFA";
        public const string MODE_SOLO               = "SOLO";
        public const string MODE_COOP               = "COOP";

        public const string ROLE_DRAWER             = "Drawer";
        public const string ROLE_GUESSER            = "Guesser";


        //SOCKET
        public const string LOGIN_EVENT    = "login";
        public const string MESSAGE_EVENT  = "chat";
        public const string LOGOUT_EVENT   = "logout";
        public const string LOGGING_EVENT  = "logging";
    }
}
