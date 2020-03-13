using Newtonsoft.Json;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.VueModeles
{
    class LobbyViewModel: BaseViewModel, IPageViewModel
    {
        public string LobbyName { get; set; }
        private ObservableCollection<string> _usernames;
        public ObservableCollection<string> Usernames
        {
            get { return _usernames; }
            set { _usernames = value; ProprieteModifiee(); }
        }

        public LobbyViewModel(string lobbyname)
        {
            Usernames = new ObservableCollection<string>();
            this.LobbyName = lobbyname;
            Mediator.Subscribe("joinLobby", refreshUserList);
            fetchUsername();
        }

        public void refreshUserList(object lobbyName)
        {
            if((string)lobbyName == this.LobbyName)
            {
                fetchUsername();
            }
        }
        private async void fetchUsername()
        {
            ObservableCollection<string> usernames = new ObservableCollection<string>();
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.USERS_LOBBY_PATH + LobbyName);//TODO

            StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
            String responseData = streamReader.ReadToEnd();
            var myData = JsonConvert.DeserializeObject<List<String>>(responseData);
            foreach (var item in myData)
            {
                App.Current.Dispatcher.Invoke(delegate
                {
                    usernames.Add(item);
                });
            }
            Usernames = usernames;
        }
    }
}
