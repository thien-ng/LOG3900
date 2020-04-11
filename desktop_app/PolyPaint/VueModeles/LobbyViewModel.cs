using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class LobbyViewModel : BaseViewModel, IPageViewModel
    {
        public LobbyViewModel(string lobbyname, string mode)
        {
            Usernames = new ObservableCollection<UserLobby>();
            OnlineUsers = new ObservableCollection<string>();
            this.LobbyName = lobbyname;
            this.Mode = mode;
            _isStartGameVisible = false;
            _searchString = "";
            _isAddBotPossible = false;
            fetchUsername();
            getOnlineUsers();
            ServerService.instance.socket.On("lobby-notif", data => refreshUserList((JObject)data));
            ServerService.instance.socket.On("game-start", joingame);
            ServerService.instance.socket.On("lobby-kicked", kickedFromLobby);

            Bots = new ObservableCollection<string> { "bot:sebastien", "bot:olivia", "bot:olivier" };
        }


        #region Public Attributes

        public static string[] BotList = { "bot:sebastien", "bot:olivia", "bot:olivier" };
        public string LobbyName { get; set; }
        public string Mode { get; set; }
        private ObservableCollection<UserLobby> _usernames;
        public ObservableCollection<UserLobby> Usernames
        {
            get { return _usernames; }
            set 
            { 
                _usernames = value;
                ProprieteModifiee();
                if (Mode == Constants.MODE_FFA)
                {
                    IsStartGameVisible = (_usernames.Count < Constants.MIN_MODE_FFA) ? false : true;
                }
            }
        }

        private ObservableCollection<string> _bots;
        public ObservableCollection<string> Bots
        {
            get { return _bots; }
            set { _bots = value; ProprieteModifiee(); }
        }

        private ObservableCollection<string> _onlineUsers;
        public ObservableCollection<string> OnlineUsers
        {
            get { return _onlineUsers; }
            set { _onlineUsers = value; ProprieteModifiee(); }
        }

        private string _searchString;
        public string SearchString
        {
            get { return _searchString; }
            set
            {
                _searchString = value;
                getOnlineUsers();
                ProprieteModifiee();
            }
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
                switch (Mode)
                {
                    case Constants.MODE_COOP:
                        IsAddBotPossible = false;
                        IsStartGameVisible = true;
                        IsInvitePossible = true;
                        break;
                    case Constants.MODE_SOLO:
                        IsAddBotPossible = false;
                        IsStartGameVisible = true;
                        IsInvitePossible = false;
                        break;
                    default:
                        if (value)
                        {
                            IsAddBotPossible = true;
                            IsInvitePossible = true;
                        }
                        else
                            IsAddBotPossible = false;

                        break;
                }
                
            }
        }

        private bool _isAddBotPossible;
        public bool IsAddBotPossible
        {
            get { return _isAddBotPossible; }
            set
            {
                _isAddBotPossible = value;
                ProprieteModifiee();
            }
        }

        private bool _isInvitePossible;
        public bool IsInvitePossible
        {
            get { return _isInvitePossible; }
            set
            {
                _isInvitePossible = value;
                ProprieteModifiee();
            }
        }

        private bool _isStartGameVisible;
        public bool IsStartGameVisible
        {
            get { return _isStartGameVisible; }
            set
            {
                if(IsGameMaster)
                    _isStartGameVisible = value;
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

        private bool _isInviteUserDialogOpen;
        public bool IsInviteUserDialogOpen
        {
            get { return _isInviteUserDialogOpen; }
            set
            {
                if (_isInviteUserDialogOpen == value) return;
                _isInviteUserDialogOpen = value;
                ProprieteModifiee();
            }
        }

        private object _inviteDialogContent;
        public object InviteDialogContent
        {
            get { return _inviteDialogContent; }
            set
            {
                if (_inviteDialogContent == value) return;
                _inviteDialogContent = value;
                ProprieteModifiee();
            }
        }
        #endregion

        #region Methods
        private void kickedFromLobby()
        {
            App.Current.Dispatcher.Invoke(delegate
            {
                Mediator.Notify("LeaveLobby", "");
                MessageBox.Show("You got kicked from lobby");
            });
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
                Mediator.Notify("LeaveLobby", "");
            }
        }

        private async Task kickPlayer(string username)
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_LEAVE_PATH;
            dynamic values = new JObject();
            values.username = username;
            values.lobbyName = LobbyName;
            values.isKicked = true;
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
            if((string)data.GetValue("lobbyName") == this.LobbyName && ((string)data.GetValue("type") == "join"))
            {
                fetchUsername();
            }
            if((string)data.GetValue("type") == "leave" && (string)data.GetValue("lobbyName") == this.LobbyName)
            {
                if (data.GetValue("username").ToString().Contains("bot:"))
                {
                    App.Current.Dispatcher.Invoke(delegate
                    {
                        Bots.Add(data.GetValue("username").ToString());
                    });
                }
                fetchUsername();
            }
        }
        private async void fetchUsername()
        {
            App.Current.Dispatcher.Invoke(delegate
            {
                Usernames.Clear();
            });
            ObservableCollection<UserLobby> usernames = new ObservableCollection<UserLobby>();
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
                        usernames.Add(new UserLobby(item, item == ServerService.instance.username));
                    });
                }
                Usernames = usernames;
                string firstUser = findFirstNotBot(Usernames);
                IsGameMaster = ServerService.instance.username == firstUser;
            }
        }

        private string findFirstNotBot(ObservableCollection<UserLobby> list)
        {
            ObservableCollection<UserLobby> temp = new ObservableCollection<UserLobby>(list);
            while (temp.First<UserLobby>().username.Contains("bot:"))
            {
                temp.Remove(temp.First<UserLobby>());
            }
            return temp.First<UserLobby>().username;
        }

        private async void getOnlineUsers()
        {
            OnlineUsers.Clear();
            ObservableCollection<string> usernames = new ObservableCollection<string>();
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.ONLINE_USERS_PATH + SearchString);
            if (response.IsSuccessStatusCode)
            {
                StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
                String responseData = streamReader.ReadToEnd();

                var myData = JsonConvert.DeserializeObject<List<String>>(responseData);
                foreach (var item in myData)
                {
                    App.Current.Dispatcher.Invoke(delegate
                    {
                        if(item!=ServerService.instance.username)
                            usernames.Add(item);
                    });
                }
                OnlineUsers = usernames;
            }
        }

        private async Task startGame()
        {
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.START_GAME_PATH + LobbyName);
            if (response.IsSuccessStatusCode)
            {
                Mediator.Notify("GoToGameScreen", Mode);
            }
        }

        private void joingame()
        {
            if(!IsGameMaster)
                Mediator.Notify("GoToGameScreen", Mode);
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
        private async Task InviteUserAsync(string x)
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_INVITE_PATH;
            dynamic values = new JObject();
            values.username = x;
            values.lobbyName = this.LobbyName;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if (response.IsSuccessStatusCode)
                MessageBox.Show("Invite sucessfully sent");
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
                    await startGame();
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
        
        private ICommand _openInviteUserControl;
        public ICommand OpenInviteUserControl
        {
            get
            {
                return _openInviteUserControl ?? (_openInviteUserControl = new RelayCommand(x =>
                {
                    SearchString = "";
                    InviteDialogContent = new InviteUserControl();
                    IsInviteUserDialogOpen = true;
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

        private ICommand _inviteUserCommand;
        public ICommand InviteUserCommand
        {
            get
            {
                return _inviteUserCommand ?? (_inviteUserCommand = new RelayCommand(async x =>
                {
                    IsInviteUserDialogOpen = false;
                    await Task.Run(() => InviteUserAsync((string)x));
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
