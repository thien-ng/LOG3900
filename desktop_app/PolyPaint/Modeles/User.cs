using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class User
    {
        public string _username;
        public string _password;
        public User(string username, string password)
        {
            _username = username;
            _password = password;
        }
    }
}
