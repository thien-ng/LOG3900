using MaterialDesignThemes.Wpf;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class HomeViewModel : BaseViewModel, IPageViewModel, IDisposable
    { 
        public LobbyViewModel LobbyViewModel { get; private set; }

        public GameViewModel GameViewModel { get; private set; }
        public GamelistViewModel GamelistViewModel { get; }
        public ProfileViewModel ProfileViewModel { get; }
        public HomeViewModel()
        {
            GamelistViewModel = new GamelistViewModel();
            ProfileViewModel = new ProfileViewModel();

            Setup();
        }

        #region Public Attributes

        public enum Views
        {
            Gamelist,
            Profile,
            Lobby,
            Game
        }

        public string Lobbyname { get; set; }
        public string Mode_Invited { get; set; }

        private ObservableCollection<MessageChannel> _subChannels;
        public ObservableCollection<MessageChannel> SubChannels
        {
            get { return _subChannels; }
            set { _subChannels = value; ProprieteModifiee(); }
        }

        private ObservableCollection<MessageChannel> _notSubChannels;
        public ObservableCollection<MessageChannel> NotSubChannels
        {
            get { return _notSubChannels; }
            set { _notSubChannels = value; ProprieteModifiee(); }
        }

        private ChatRoom _selectedChannel;
        public ObservableCollection<MessageChat> Messages
        {
            get { return _selectedChannel.Messages; }
        }

        public ObservableCollection<MessageGame> MessagesGame
        {
            get { return _selectedChannel.MessagesGame; }
        }

        private string _lobbyInvitedTo;
        public string LobbyInvitedTo
        {
            get { return _lobbyInvitedTo; }
            set { _lobbyInvitedTo = value; ProprieteModifiee(); }
        }

        private string _pendingMessage;
        public string PendingMessage
        {
            get { return _pendingMessage; }
            set { _pendingMessage = value; ProprieteModifiee(); }
        }

        private string _searchString;
        public string SearchString
        {
            get { return _searchString; }
            set
            {
                _searchString = value;
                FilterChannels();
                ProprieteModifiee();
            }
        }

        private Views _switchView;
        public Views SwitchView
        {
            get { return _switchView; }
            set { _switchView = value; ProprieteModifiee();
                if (_switchView == Views.Lobby){
                    IsNotInLobby = false;
                } }
        }

        private string _switchViewButton;
        public string SwitchViewButton
        {
            get { return _switchViewButton; }
            set { _switchViewButton = value; ProprieteModifiee(); }
        }

        private string _switchViewButtonTooltip;
        public string SwitchViewButtonTooltip
        {
            get { return _switchViewButtonTooltip; }
            set { _switchViewButtonTooltip = value; ProprieteModifiee(); }
        }

        private bool _frontEnabled;
        public bool FrontEnabled
        {
            get { return _frontEnabled; }
            set { _frontEnabled = value; ProprieteModifiee(); }
        }

        private bool _backEnabled;
        public bool BackEnabled
        {
            get { return _backEnabled; }
            set { _backEnabled = value; ProprieteModifiee(); }
        }

        private bool _isNotInLobby;
        public bool IsNotInLobby
        {
            get { return _isNotInLobby; }
            set { _isNotInLobby = value; ProprieteModifiee(); }
        }

        private bool _isCreateChannelDialogOpen;
        public bool IsCreateChannelDialogOpen
        {
            get { return _isCreateChannelDialogOpen; }
            set
            {
                if (_isCreateChannelDialogOpen == value) return;
                _isCreateChannelDialogOpen = value;
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

        private bool _isInvitedDialogOpen;
        public bool IsInvitedDialogOpen
        {
            get { return _isInvitedDialogOpen; }
            set
            {
                if (_isInvitedDialogOpen == value) return;
                _isInvitedDialogOpen = value;
                ProprieteModifiee();
            }
        }

        private object _invitedDialogContent;
        public object InvitedDialogContent
        {
            get { return _invitedDialogContent; }
            set
            {
                if (_invitedDialogContent == value) return;
                _invitedDialogContent = value;
                ProprieteModifiee();
            }
        }

        private string _newChannelString;
        public string NewChannelString
        {
            get { return _newChannelString; }
            set
            {
                if (_newChannelString == value) return;
                _newChannelString = value;
                ProprieteModifiee();
            }
        }

        #endregion

        #region Methods

        private void Setup()
        {
            Mediator.Subscribe("ChangeChannel", ChangeChannel);
            Mediator.Subscribe("SubToChannel", SubToChannel);
            Mediator.Subscribe("UnsubChannel", UnsubChannel);
            Mediator.Subscribe("GoToLobbyScreen", goToLobbyView);
            Mediator.Subscribe("GoToGameScreen", goToGameView);
            Mediator.Subscribe("LeaveLobby", goToGameListView);

            _subChannels = new ObservableCollection<MessageChannel>();
            _notSubChannels = new ObservableCollection<MessageChannel>();
            _selectedChannel = new ChatRoom(Constants.DEFAULT_CHANNEL, false);

            FetchChannels();
            
            _switchView = Views.Gamelist;
            _switchViewButton = "Profile";
            _switchViewButtonTooltip = "Access to profile";
            _isNotInLobby = true;
            _frontEnabled = false;
            _backEnabled = false;
            _searchString = "";
            _lobbyInvitedTo = "";

            ServerService.instance.socket.On("channel-new", data => UpdateUnsubChannel((JObject)data));
            ServerService.instance.socket.On("channel-delete", data => RemoveUnsubChannel((JObject)data));
            ServerService.instance.socket.On("lobby-invitation", data => processLobbyInvite((JObject)data));
        }

        private void processLobbyInvite(JObject data)
        {
            LobbyInvitedTo = data.GetValue("lobbyName").ToString();
            Mode_Invited = data.GetValue("mode").ToString();
            Application.Current.Dispatcher.Invoke(delegate
            { 
                InvitedDialogContent = new InvitedUserControl();
                IsInvitedDialogOpen = true;
            });
        }

        private void goToGameListView(object obj)
        {
            if (LobbyViewModel != null)
            {
                LobbyViewModel.Dispose();
                LobbyViewModel = null;
            }
            if (GameViewModel != null)
            {
                GameViewModel.Dispose();
                GameViewModel = null;
            }

            GamelistViewModel.SubscribeLobbyNotif();
            GamelistViewModel.getLobbies();

            SwitchView = Views.Gamelist;
            IsNotInLobby = true;
            Application.Current.Dispatcher.Invoke(delegate
            {
                FetchChannels();
            });
        }
        private void goToGameView(object obj)
        {
            if (LobbyViewModel != null)
            {
                LobbyViewModel.Dispose();
                LobbyViewModel = null;
            }
            Task.Delay(500).ContinueWith(_ =>
            {
                if (GameViewModel != null)
                {
                    GameViewModel.Dispose();
                    GameViewModel = null;
                }
                GameViewModel = new GameViewModel((string)obj);
                App.Current.Dispatcher.Invoke(delegate
                {
                    SubChannels.Remove(_subChannels.SingleOrDefault(i => i.id == (Constants.LOBBY_CHANNEL + Lobbyname)));
                    SubChannels.Add(new MessageChannel(Constants.GAME_CHANNEL, true, false));
                    ChangeChannel(Constants.GAME_CHANNEL);
                });
                SwitchView = Views.Game;
            });
        }


        private void goToLobbyView(object lobbyData)
        {
            IsNotInLobby = false;
            Dictionary<string, string> data = new Dictionary<string, string>((Dictionary<string, string>)lobbyData);
            if (LobbyViewModel != null)
            {
                LobbyViewModel.Dispose();
                LobbyViewModel = null;
            }

            LobbyViewModel = new LobbyViewModel(data["lobbyName"], data["mode"]);
            this.Lobbyname = data["lobbyName"];
            string lobbyChannel = Constants.LOBBY_CHANNEL + this.Lobbyname;
            SwitchView = Views.Lobby;
            Application.Current.Dispatcher.Invoke(delegate
            {
               SubChannels.Add(new MessageChannel(lobbyChannel, true, true));
            });
            ChangeChannel(lobbyChannel);
        }

        private async void joinLobbyFromInvite(string lobbyInvitedTo)
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.Add("isPrivate", false);
            values.lobbyName = lobbyInvitedTo;
            values.password = "";
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if ((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                Dictionary<string, string> data = new Dictionary<string, string>();
                data.Add("lobbyName", lobbyInvitedTo);
                data.Add("mode", Mode_Invited);
                Mediator.Notify("GoToLobbyScreen", data);
            }
        }

        public void FetchChannels()
        {
            Application.Current.Dispatcher.Invoke(async delegate
            {
                 SubChannels.Clear();
                 NotSubChannels.Clear();


                 var subChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
                 var notSubChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.NOT_SUB_CHANNELS_PATH + "/" + ServerService.instance.username);

                 ProcessChannelRequest(subChannelReq, SubChannels, true);
                 ProcessChannelRequest(notSubChannelReq, NotSubChannels, false);

                 ChangeChannel(Constants.DEFAULT_CHANNEL);
                _subChannels.SingleOrDefault(i => i.id == Constants.DEFAULT_CHANNEL).isSelected = true;

            });

        }

        private async void ProcessChannelRequest(HttpResponseMessage response, ObservableCollection<MessageChannel> list, bool isSubbed)
        {
            Application.Current.Dispatcher.Invoke(delegate
            {
                list.Clear();
            });
            if (response.IsSuccessStatusCode)
            {
                JArray responseJson = JArray.Parse(await response.Content.ReadAsStringAsync());
                foreach (JObject item in responseJson)
                {
                    if (item.ContainsKey("id"))
                    {
                        Application.Current.Dispatcher.Invoke(delegate
                        {
                            list.Add(new MessageChannel(item.GetValue("id").ToString(), isSubbed, false));
                        });
                    }
                }
            }
        }

        private void ChangeChannel(object id)
        {
            string channelId = (string)id;


            if (channelId != _selectedChannel.ID || channelId == Constants.DEFAULT_CHANNEL)
            {
                MessageChannel channel;

                channel = _subChannels.SingleOrDefault(i => i.id == _selectedChannel.ID);

                if (channel != null)
                    channel.isSelected = false;

                _selectedChannel = new ChatRoom((string)id, _subChannels.SingleOrDefault(i => i.id == channelId).isLobbyChat);
                _subChannels.SingleOrDefault(i => i.id == _selectedChannel.ID).isSelected = true;
                ProprieteModifiee("Messages");
                ProprieteModifiee("MessagesGame");

            }
        }

        private async void SubToChannel(object id)
        {
            string channelId = (string)id;
            string requestPath = Constants.SERVER_PATH + Constants.JOIN_CHANNEL_PATH + "/" + ServerService.instance.username + "/" + channelId;
            var response = await ServerService.instance.client.PutAsync(requestPath, new StringContent(""));

            if (!response.IsSuccessStatusCode)
            {
                ShowMessageBox("Error while joining channel");
                return;
            }

            JObject responseJson = JObject.Parse(await response.Content.ReadAsStringAsync());

            if (!(responseJson.ContainsKey("status") && responseJson.ContainsKey("message")))
            {
                ShowMessageBox("Error parsing server response");
                return;
            }

            if (responseJson.GetValue("status").ToString() == "200")
            {
                Application.Current.Dispatcher.Invoke(delegate
                {
                    _notSubChannels.Remove(_notSubChannels.SingleOrDefault(i => i.id == channelId));
                    _subChannels.Add(new MessageChannel(channelId, true, false));
                    ChangeChannel(channelId);
                });
            }
            else
                ShowMessageBox(responseJson.GetValue("message").ToString());
        }

        private async void UnsubChannel(object id)
        {
            string channelId = (string)id;
            string requestPath = Constants.SERVER_PATH + Constants.LEAVE_CHANNEL_PATH + "/" + ServerService.instance.username + "/" + channelId;
            var response = await ServerService.instance.client.DeleteAsync(requestPath);

            if (!response.IsSuccessStatusCode)
            {
                ShowMessageBox("Error while leaving channel");
                return;
            }

            JObject responseJson = JObject.Parse(await response.Content.ReadAsStringAsync());

            if (!(responseJson.ContainsKey("status") && responseJson.ContainsKey("message")))
            {
                ShowMessageBox("Error parsing server response");
                return;
            }

            if (responseJson.GetValue("status").ToString() == "200")
            {
                if (_selectedChannel.ID == channelId)
                    ChangeChannel(Constants.DEFAULT_CHANNEL);

                MessageChannel leftChannel = new MessageChannel(channelId, false, false);
                _subChannels.Remove(_subChannels.SingleOrDefault(i => i.id == channelId));

                await Application.Current.Dispatcher.Invoke(async delegate
                {
                    NotSubChannels.Clear();
                    var notSubChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.NOT_SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
                    ProcessChannelRequest(notSubChannelReq, NotSubChannels, false);

                });
            }
            else
                ShowMessageBox(responseJson.GetValue("message").ToString());
        }

        private void UpdateUnsubChannel(JObject channelMes) 
        {
            MessageChannel newChannel = new MessageChannel(channelMes.GetValue("id").ToString(), false, false);
            App.Current.Dispatcher.Invoke(delegate
            {
                _notSubChannels.Add(newChannel);
            });
        }

        private void RemoveUnsubChannel(JObject channelMes)
        {
            string channelId = channelMes.GetValue("channel").ToString();
            App.Current.Dispatcher.Invoke(delegate
            {
                try 
                { 
                    _notSubChannels.Remove(_notSubChannels.Where(i => i.id == channelId).Single());
                }
                catch(InvalidOperationException e) 
                {
                    // Fade away silently
                }
            });
        }

        private void Disconnect()
        {
            ServerService.instance.socket.Emit("logout");
            ServerService.instance.username = "";
            ServerService.instance.user = null;
            Mediator.Notify("GoToLoginScreen", "");
            Dispose();
        }


        private void FilterChannels()
        {
            string path = Constants.SERVER_PATH + Constants.SEARCH_CHANNEL_PATH + "/" + ServerService.instance.username + "/" + _searchString;
            ServerService.instance.client.GetAsync(path).ContinueWith(responseTask => 
            {
                var response = responseTask.Result;
                response.Content.ReadAsStringAsync().ContinueWith(jsonTask =>
                {
                    var jArray = JArray.Parse(jsonTask.Result);

                    Application.Current.Dispatcher.Invoke(delegate
                    {
                        _subChannels.Clear();
                        _notSubChannels.Clear();

                        foreach (JObject item in jArray)
                        {
                            if (!(item.ContainsKey("id") && item.ContainsKey("sub")))
                                continue;

                            if (item.GetValue("sub").Value<bool>() == true)
                                _subChannels.Add(new MessageChannel(item.GetValue("id").ToString(), true, false));
                            else
                                _notSubChannels.Add(new MessageChannel(item.GetValue("id").ToString(), false, false));
                        }
                    });
                });
            });
        }

        private async void declineInvite(string lobbyInvitedTo)
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_REFUSE_INVITE_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.lobbyName = lobbyInvitedTo;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
        }

        public override void Dispose()
        {
            GameViewModel = null;
            LobbyViewModel = null;
            ServerService.instance.socket.Off("channel-new");
            ServerService.instance.socket.Off("lobby-invitation");
            Mediator.Unsubscribe("ChangeChannel", ChangeChannel);
            Mediator.Unsubscribe("SubToChannel", SubToChannel);
            Mediator.Unsubscribe("UnsubChannel", UnsubChannel);
            Mediator.Unsubscribe("GoToLobbyScreen", goToLobbyView);
            Mediator.Unsubscribe("GoToGameScreen", goToGameView);
            Mediator.Unsubscribe("LeaveLobby", goToGameListView);
            GC.Collect();
        }
        
        private void ShowMessageBox(string message)
        {
            App.Current.Dispatcher.Invoke(delegate
            {
                MessageBoxDisplayer.ShowMessageBox(message);
            });
        }

        #endregion

        #region Commands

        private ICommand _disconnectCommand;
        public ICommand DisconnectCommand
        {
            get
            {
                
                return _disconnectCommand ?? (_disconnectCommand = new RelayCommand(x => Disconnect()));
            }
        }

        private ICommand _sendCommand;
        public ICommand SendCommand
        {
            get
            {
                return _sendCommand ?? (_sendCommand = new RelayCommand<string>(x =>
                {
                    _selectedChannel.SendMessage(PendingMessage);
                    PendingMessage = "";
                }));
            }
        }

        private ICommand _switchViewCommand;
        public ICommand SwitchViewCommand
        {
            get
            {
                return _switchViewCommand ?? (_switchViewCommand = new RelayCommand(x =>
                {
                    SwitchView = SwitchView == Views.Gamelist ? Views.Profile : Views.Gamelist;
                    if (SwitchView == Views.Gamelist)
                        GamelistViewModel.getLobbies();
                    if (SwitchView == Views.Profile)
                        ProfileViewModel.fetchProfile();
                    SwitchViewButton = SwitchViewButton == "Profile" ? "GameList" : "Profile";
                    SwitchViewButtonTooltip = SwitchViewButtonTooltip == "Access to profile" ? "Access to gameList" : "Access to profile";
                    if (!FrontEnabled && !BackEnabled)
                    {
                        FrontEnabled = true;
                    }
                    FrontEnabled = BackEnabled;
                    BackEnabled = !BackEnabled;
                }));
            }
        }

        private ICommand _addChannelCommand;
        public ICommand AddChannelCommand
        {
            get
            {
                return _addChannelCommand ?? (_addChannelCommand = new RelayCommand(x =>
                {
                    DialogContent = new CreateChannelControl();
                    IsCreateChannelDialogOpen = true;
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
                    NewChannelString = "";
                    IsCreateChannelDialogOpen = false;
                }));
            }
        }

        private ICommand _acceptInviteCommand;
        public ICommand AcceptInviteCommand
        {
            get
            {
                return _acceptInviteCommand ?? (_acceptInviteCommand = new RelayCommand(x =>
                {
                    joinLobbyFromInvite(LobbyInvitedTo);
                    IsInvitedDialogOpen = false;
                }));
            }
        }


        private ICommand _declineInviteCommand;
        public ICommand DeclineInviteCommand
        {
            get
            {
                return _declineInviteCommand ?? (_declineInviteCommand = new RelayCommand(x =>
                {
                    declineInvite(LobbyInvitedTo);
                    LobbyInvitedTo = "";
                    IsInvitedDialogOpen = false;
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
                    
                    if (String.Equals(NewChannelString, Constants.GAME_CHANNEL, StringComparison.OrdinalIgnoreCase))
                        ShowMessageBox("This channel name is used for in game chat.");
                    else
                        await Task.Run(() => SubToChannel(NewChannelString));
                    NewChannelString = "";
                    IsCreateChannelDialogOpen = false;
                }));
            }
        }

        private ICommand _createGameCommand;
        public ICommand CreateGameCommand
        {
            get
            {
                return _createGameCommand ?? (_createGameCommand = new RelayCommand(async x =>
                {
                    var view = new CreateGameControl { DataContext = new CreateGameViewModel() };

                    await DialogHost.Show(view, "CreateGameDialog", ClosingEventHandler);
                }));
            }
        }

        private void ClosingEventHandler(object sender, DialogClosingEventArgs args)
        {
            if (args.Parameter == null) return;

            JObject parameters = (JObject)args.Parameter;

            if ((bool)parameters.SelectToken("IsAccept") == false) return;

            //await getGameCards();
        }



        #endregion

    }
}
