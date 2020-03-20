using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

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
            fetchUsername();
            ServerService.instance.socket.On("lobby-notif", refreshUserList);

        }

        private void refreshUserList()
        {
            //if((string)lobbyName == this.LobbyName) TODO
            //{
                fetchUsername();
            //}
        }
        private async void fetchUsername()
        {
            ObservableCollection<string> usernames = new ObservableCollection<string>();
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.USERS_LOBBY_PATH + LobbyName);
            if (response.IsSuccessStatusCode)
            {
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
                IsGameMaster = ServerService.instance.username == Usernames.First<string>();
            }
        }

        private bool _isGameMaster;
        public bool IsGameMaster
        {
            get { return _isGameMaster; }
            set
            {
                _isGameMaster = value;
                ProprieteModifiee();
            }
        }

        private ICommand _startGameCommand;
        public ICommand StartGameCommand
        {
            get
            {
                return _startGameCommand ?? (_startGameCommand = new RelayCommand(async x =>
                {
                    await Task.Run(() => startGame());
                }));
            }
        }

        private async Task startGame()
        {
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.START_GAME_PATH + LobbyName);
            Mediator.Notify("GoToDrawScreen");
        }

        private ICommand _leaveLobbyCommand;
        public ICommand LeaveLobbyCommand
        {
            get
            {
                return _leaveLobbyCommand ?? (_leaveLobbyCommand = new RelayCommand(async x =>
                {
                    await Task.Run(() => leaveLobby());
                }));
            }
        }

        private async Task leaveLobby()
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_LEAVE_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.lobbyName = LobbyName;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if ((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                Mediator.Notify("LeaveLobby","");
            }
        }
    }
}
