using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Utilitaires
{
    class Constants
    {
        //SERVER
        public const string SERVER_PATH             = "http://10.200.11.4:3000";
        public const string ACCOUNT_PATH            = "/account";
        public const string LOGIN_PATH              = ACCOUNT_PATH + "/login";
        public const string REGISTER_PATH           = ACCOUNT_PATH + "/register";
        public const string CHAT_MESSAGES_PATH      = "/chat/messages";
        public const string CHANNELS_PATH           = "/chat/channels";
        public const string GAME_PATH               = "/game/lobby";
        public const string GAMECARDS_PATH          = "/card";
        public const string CARDSCREATOR_PATH       = "/creator/game/new";
        public const string GAME_JOIN_PATH          = "/game/lobby/join";
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

        public const string DEFAULT_CHANNEL = "general";

        //SOCKET
        public const string LOGIN_EVENT    = "login";
        public const string MESSAGE_EVENT  = "chat";
        public const string LOGOUT_EVENT   = "logout";
        public const string LOGGING_EVENT  = "logging";
    }
}
