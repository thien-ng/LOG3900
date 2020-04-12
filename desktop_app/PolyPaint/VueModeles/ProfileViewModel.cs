using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using Newtonsoft.Json;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;

namespace PolyPaint.VueModeles
{
    class ProfileViewModel: BaseViewModel
    {
        
        public ProfileViewModel()
        {
            _username = Services.ServerService.instance.username;
            if(ServerService.instance.user == null)
            {
                fetchProfile();
            }
            else
            {
                _firstname = Services.ServerService.instance.user.firstName;
                _lastname = Services.ServerService.instance.user.lastName;
                _connections = ServerService.instance.user.connections;
                _stats = ServerService.instance.user.stats;
                _games = ServerService.instance.user.games;
            }
        }
        #region Public Attributes

        string _username;
        public string Username
        {
            get { return _username; }
            set { _username = value; ProprieteModifiee(); }
        }

        string _firstname;
        public string Firstname
        {
            get { return _firstname; }
            set { _firstname = value; ProprieteModifiee(); }
        }
        string _lastname;
        public string Lastname
        {
            get { return _lastname; }
            set { _lastname = value; ProprieteModifiee(); }
        }

        public Connection[] _connections;
        public Connection[] Connections
        {
            get { return _connections; }
            set { _connections = value; ProprieteModifiee(); }
        }

        private Stats _stats;
        public Stats Stats 
        {
            get { return _stats; }
            set { _stats = value; ProprieteModifiee(); }
        }

        private Game[] _games;
        public Game[] Games 
        {
            get { return _games; }
            set { _games = value; ProprieteModifiee(); }
        }

        #endregion


        #region Methods

        public async void fetchProfile()
        {
            try
            {
                var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.USER_INFO_PATH + ServerService.instance.username);
                if (response.IsSuccessStatusCode)
                {
                    string responseString = await response.Content.ReadAsStringAsync();
                    var data = JsonConvert.DeserializeObject<User>(responseString);
                    ServerService.instance.user = data;
                    Firstname = Services.ServerService.instance.user.firstName;
                    Lastname = Services.ServerService.instance.user.lastName;
                    Connections = ServerService.instance.user.connections;
                    Stats = ServerService.instance.user.stats;
                    Games = ServerService.instance.user.games;
                }
            }
            catch (Exception)
            {
                MessageBox.Show("failed to retrieve stats");
            }

        }

        #endregion
    }
}
