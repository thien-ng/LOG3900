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
        public const string SERVER_PATH    = "http://10.200.30.118:3000";
        public const string ACCOUNT_PATH   = "/account";
        public const string LOGIN_PATH     = ACCOUNT_PATH + "/login";
        public const string REGISTER_PATH  = ACCOUNT_PATH + "/register";
        public const int    SUCCESS_CODE   = 200;
        public const int    PWD_MIN_LENGTH = 1; //TODO
        public const int    USR_MIN_LENGTH = 1; //TODO

        //SOCKET
        public const string LOGIN_EVENT    = "login";
        public const string MESSAGE_EVENT  = "chat";


        public static class Vues
        {
            public const int Login = 0;
            public const int Register = 1;
            public const int Draw = 2;
            public const int Chat = 3;
        }
    }
}
