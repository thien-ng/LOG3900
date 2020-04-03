using MaterialDesignThemes.Wpf;
using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class HomeViewModel : BaseViewModel, IPageViewModel
    { 
        public LobbyViewModel LobbyViewModel { get; private set; }

        public GameViewModel GameViewModel { get; private set; }
        public string Lobbyname { get; set; }
        public HomeViewModel()
        {
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

            FetchChannels();
            
            _switchView = Views.Gamelist;
            _switchViewButton = "Profile";
            _switchViewButtonTooltip = "Access to profile";
            _isNotInLobby = true;
            _frontEnabled = false;
            _backEnabled = false;
            _selectedChannel = new ChatRoom(Constants.DEFAULT_CHANNEL, false);
            _searchString = "";

            ServerService.instance.socket.On("channel-new", data => UpdateUnsubChannel((JObject)data));
        }

        private void goToGameListView(object obj)
        {
            SwitchView = Views.Gamelist;
            IsNotInLobby = true;
            Application.Current.Dispatcher.Invoke(delegate
            {
                FetchChannels();
            });
        }
        private void goToGameView(object obj)
        {
            SwitchView = Views.Game;
            GameViewModel = new GameViewModel();
            Application.Current.Dispatcher.Invoke(delegate
            {
                _subChannels.Remove(_subChannels.SingleOrDefault(i => i.id == Lobbyname));
                _subChannels.Add(new MessageChannel(Constants.GAME_CHANNEL, true, false));
            });
            ChangeChannel(Constants.GAME_CHANNEL);
        }


        private void goToLobbyView(object lobbyname)
        {
            IsNotInLobby = false;
            LobbyViewModel = new LobbyViewModel((string)lobbyname);
            this.Lobbyname = (string)lobbyname;
            string lobbyChannel = "Lobby :" + this.Lobbyname;
            SwitchView = Views.Lobby;
            Application.Current.Dispatcher.Invoke(delegate
            {
               _subChannels.Add(new MessageChannel(lobbyChannel, true, true));
            });
            ChangeChannel(lobbyChannel);
        }

        private async void FetchChannels()
        {
            _subChannels.Clear();
            _notSubChannels.Clear();
            
            var subChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
            var notSubChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.NOT_SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
            
            ProcessChannelRequest(subChannelReq, _subChannels, true);
            ProcessChannelRequest(notSubChannelReq, _notSubChannels, false);

            _subChannels.SingleOrDefault(i => i.id == Constants.DEFAULT_CHANNEL).isSelected = true;
        }

        private async void ProcessChannelRequest(HttpResponseMessage response, ObservableCollection<MessageChannel> list, bool isSubbed)
        {
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


            if (channelId != _selectedChannel.ID)
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
                MessageBox.Show("Error while joining channel");
                return;
            }

            JObject responseJson = JObject.Parse(await response.Content.ReadAsStringAsync());

            if (!(responseJson.ContainsKey("status") && responseJson.ContainsKey("message")))
            {
                MessageBox.Show("Error parsing server response");
                return;
            }

            if (responseJson.GetValue("status").ToString() == "200")
            {
                Application.Current.Dispatcher.Invoke(delegate
                {
                    _notSubChannels.Remove(_notSubChannels.SingleOrDefault(i => i.id == channelId));
                    _subChannels.Add(new MessageChannel(channelId, true, false));
                });
            }
            else
                MessageBox.Show(responseJson.GetValue("message").ToString());
        }

        private async void UnsubChannel(object id)
        {
            string channelId = (string)id;
            string requestPath = Constants.SERVER_PATH + Constants.LEAVE_CHANNEL_PATH + "/" + ServerService.instance.username + "/" + channelId;
            var response = await ServerService.instance.client.DeleteAsync(requestPath);

            if (!response.IsSuccessStatusCode)
            {
                MessageBox.Show("Error while leaving channel");
                return;
            }

            JObject responseJson = JObject.Parse(await response.Content.ReadAsStringAsync());

            if (!(responseJson.ContainsKey("status") && responseJson.ContainsKey("message")))
            {
                MessageBox.Show("Error parsing server response");
                return;
            }

            if (responseJson.GetValue("status").ToString() == "200")
            {
                if (_selectedChannel.ID == channelId)
                    ChangeChannel(Constants.DEFAULT_CHANNEL);

                MessageChannel leftChannel = new MessageChannel(channelId, false, false);
                _subChannels.Remove(_subChannels.SingleOrDefault(i => i.id == channelId));
                _notSubChannels.Add(leftChannel);
            }
            else
                MessageBox.Show(responseJson.GetValue("message").ToString());
        }

        private void UpdateUnsubChannel(JObject channelMes) 
        {
            MessageChannel newChannel = new MessageChannel(channelMes.GetValue("id").ToString(), false, false);
            App.Current.Dispatcher.Invoke(delegate
            {
                _notSubChannels.Add(newChannel);
            });
        }

        private void Disconnect()
        {
            ServerService.instance.socket.Emit("logout");
            ServerService.instance.username = "";
            ServerService.instance.user = null;
            Mediator.Notify("GoToLoginScreen", "");
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

        private ICommand _acceptCommand;
        public ICommand AcceptCommand
        {
            get
            {
                return _acceptCommand ?? (_acceptCommand = new RelayCommand(async x =>
                {
                    
                    if (String.Equals(NewChannelString, Constants.GAME_CHANNEL, StringComparison.OrdinalIgnoreCase))
                        MessageBox.Show("This channel name is used for in game chat.");
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

                    await DialogHost.Show(view, "RootDialog", ClosingEventHandler);
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
