using Newtonsoft.Json.Linq;
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
        public string _firstName;
        public string _lastName;
        public Connection[] _connections;
        public User(string username, string firstname, string lastname, Connection[] connections)
        {
            _username = username;
            _firstName = firstname;
            _lastName = lastname;
            _connections = connections;
        }
    }
}
