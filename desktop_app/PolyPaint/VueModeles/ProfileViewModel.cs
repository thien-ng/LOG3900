using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PolyPaint.Modeles;
using PolyPaint.Services;

namespace PolyPaint.VueModeles
{
    class ProfileViewModel
    {
        
        public ProfileViewModel()
        {
            _username = Services.ServerService.instance.username;
            _firstname = Services.ServerService.instance.user._firstName;
            _lastname = Services.ServerService.instance.user._lastName;
            _connections = ServerService.instance.user._connections;

        }
        #region Public Attributes

        string _username;
        public string Username
        {
            get { return _username; }
        }

        string _firstname;
        public string Firstname
        {
            get { return _firstname; }
        }
        string _lastname;
        public string Lastname
        {
            get { return _lastname; }
        }

        public Connection[] _connections;
        public Connection[] Connections
        {
            get { return _connections; }
        }
        #endregion
    }
}
