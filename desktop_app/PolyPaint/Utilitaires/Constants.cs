using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Utilitaires
{
    class Constants
    {
        public const string SERVER_PATH   = "http://72.53.102.93:3000";
        public const string ACCOUNT_PATH  = "/account";
        public const string LOGIN_PATH    = ACCOUNT_PATH + "/login";
        public const string REGISTER_PATH = ACCOUNT_PATH + "/register";

        public static class Vues
        {
            public const int Login = 0;
            public const int Register = 1;
            public const int Draw = 2;
            public const int Chat = 3;
        }
    }
}
