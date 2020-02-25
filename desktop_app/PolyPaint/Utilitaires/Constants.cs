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
        //public const string SERVER_PATH    = "http://localhost:3000";
        public const string SERVER_PATH = "https://log3000-app.herokuapp.com";
        public const string ACCOUNT_PATH   = "/account";
        public const string LOGIN_PATH     = ACCOUNT_PATH + "/login";
        public const string REGISTER_PATH  = ACCOUNT_PATH + "/register";
        public const string CHAT_MESSAGES_PATH = "/chat/messages";
        public const int    SUCCESS_CODE   = 200;
        public const int    PWD_MIN_LENGTH = 1; //TODO
        public const int    USR_MIN_LENGTH = 1; //TODO

        //SOCKET
        public const string LOGIN_EVENT    = "login";
        public const string MESSAGE_EVENT  = "chat";
        public const string LOGOUT_EVENT   = "logout";
        public const string LOGGING_EVENT  = "logging";
    }
}
