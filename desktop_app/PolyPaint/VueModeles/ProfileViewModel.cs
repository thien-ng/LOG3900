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
            if(ServerService.instance.user != null)
            {
                _firstname = Services.ServerService.instance.user.firstName;
                _lastname = Services.ServerService.instance.user.lastName;
                _connections = ServerService.instance.user.connections;
                _stats = ServerService.instance.user.stats;
                _games = ServerService.instance.user.games;
            }
            Console.WriteLine(ServerService.instance.user);
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

        private Stats _stats;
        public Stats Stats 
        {
            get { return _stats; }
        }

        private Game[] _games;
        public Game[] Games 
        {
            get { return _games; }
        }
    }
}
