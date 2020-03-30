using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
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
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class LobbyViewModel : BaseViewModel, IPageViewModel
    {
        public LobbyViewModel(string lobbyname)
        {
            Usernames = new ObservableCollection<string>();
            this.LobbyName = lobbyname;
            fetchUsername();
            ServerService.instance.socket.On("lobby-notif", data => refreshUserList((JObject)data));
            Bots = new ObservableCollection<string> { "bot:sebastien", "bot:olivia", "bot:olivier" };
        }

        #region Public Attributes

        public static string[] BotList = { "bot:sebastien", "bot:olivia", "bot:olivier" };
        public string LobbyName { get; set; }
        private ObservableCollection<string> _usernames;
        public ObservableCollection<string> Usernames
        {
            get { return _usernames; }
            set { _usernames = value; ProprieteModifiee(); }
        }

        private ObservableCollection<string> _bots;
        public ObservableCollection<string> Bots
        {
            get { return _bots; }
            set { _bots = value; ProprieteModifiee(); }
        }

        private string _selectedBot;
        public string SelectedBot
        {
            get { return _selectedBot; }
            set { _selectedBot = value; ProprieteModifiee(); }
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

        private bool _isCreateBotDialogOpen;
        public bool IsCreateBotDialogOpen
        {
            get { return _isCreateBotDialogOpen; }
            set
            {
                if (_isCreateBotDialogOpen == value) return;
                _isCreateBotDialogOpen = value;
                ProprieteModifiee();
            }
        }

        private object _dialogContent;
        public object DialogContent
        {
            get { return _dialogContent; }
            set
            {
                if (_dialogContent == value) return;
                _dialogContent = value;
                ProprieteModifiee();
            }
        }
        #endregion

        #region Methods
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
                Mediator.Notify("LeaveLobby", "");
            }
        }

        private async Task kickPlayer(string username)
        {
            Console.WriteLine(username);
            string requestPath = Constants.SERVER_PATH + Constants.GAME_LEAVE_PATH;
            dynamic values = new JObject();
            values.username = username;
            values.lobbyName = LobbyName;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if ((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                MessageBox.Show("Player " + username + " kicked.");
            }
        }

        private void refreshUserList(JObject data)
        {
            Console.WriteLine(data);
            if((string)data.GetValue("lobbyName") == this.LobbyName && ((string)data.GetValue("type") == "join"|| (string)data.GetValue("type") == "leave"))
            {
                fetchUsername();
            }
            if(!Usernames.Contains(ServerService.instance.username))
                Mediator.Notify("LeaveLobby", "");
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
        private async Task startGame()
        {
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.START_GAME_PATH + LobbyName);
            if (response.IsSuccessStatusCode)
            {
                Mediator.Notify("GoToGameScreen");
            }
        }

        private async Task processBotRequest()
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
            dynamic values = new JObject();
            values.username = SelectedBot;
            values.Add("isPrivate", false);
            values.lobbyName = this.LobbyName;
            values.password = "";
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if(response.IsSuccessStatusCode)
                Bots.Remove(SelectedBot);
        }

        #endregion

        #region Commands

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

        private ICommand _openBotControlCommand;
        public ICommand OpenBotControlCommand
        {
            get
            {
                return _openBotControlCommand ?? (_openBotControlCommand = new RelayCommand(async x =>
                {
                    DialogContent = new CreateBotControl();
                    IsCreateBotDialogOpen = true;
                }));
            }
        }

        private ICommand _cancelCommand;
        public ICommand CancelCommand
        {
            get
            {
                return _cancelCommand ?? (_cancelCommand = new RelayCommand(x =>
                {
                    IsCreateBotDialogOpen = false;
                }));
            }
        }

        private ICommand _acceptCommand;
        public ICommand AcceptCommand
        {
            get
            {
                return _acceptCommand ?? (_acceptCommand = new RelayCommand(async x =>
                {
                    await processBotRequest();
                    
                    fetchUsername();
                    IsCreateBotDialogOpen = false;
                }));
            }
        }
        private ICommand _removeUserCommand;
        public ICommand RemoveUserCommand
        {
            get
            {
                return _removeUserCommand ?? (_removeUserCommand = new RelayCommand(async x => 
                {
                    await Task.Run(() => kickPlayer((string)x));
                }));
            }
        }

        #endregion
    }
}
