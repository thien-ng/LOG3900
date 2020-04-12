using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.CompilerServices;
using System.Windows.Controls;
using System.Windows.Input;

namespace PolyPaint.Modeles
{
    public class GameCard : INotifyPropertyChanged
    {
        public GameCard(Lobby lobby)
        {
            _players = new ObservableCollection<string>();
            foreach (var item in lobby.usernames)
            {
                _players.Add(item);
            }
            _isNotFull = true;

            IsLobbyJoined = true;
            _lobby = lobby;
            _mode = lobby.mode;
            _lobbyName = lobby.lobbyName;
            Size = lobby.size;
            _actualSize = _players.Count;
            IsPrivate = lobby.isPrivate;
            _isPasswordDialogOpen = false;
            _players.CollectionChanged += this.OnCollectionChanged;
        }

        #region Public Attributes
        public event PropertyChangedEventHandler PropertyChanged;

        public PasswordBox Password { private get; set; }

        private Lobby _lobby;
        public Lobby Lobby
        {
            get { return _lobby; }
            set { _lobby = value; ProprieteModifiee(nameof(Lobby)); }
        }

        public int Size { get; }
        private int _actualSize;
        public int ActualSize 
        { 
            get { return _actualSize; }
            set
            {
                _actualSize = value; ProprieteModifiee();
            }
        }

        private string _mode;
        public string Mode { get { return _mode; } }

        private ObservableCollection<string> _players;
        public ObservableCollection<string> Players
        {
            get { return _players;  }
            set 
            {
                _players = value;
                ActualSize = Players.Count;
                ProprieteModifiee(nameof(Players));
            }
        }

        private bool _isNotFull;
        public bool IsNotFull
        {
            get { return _isNotFull; }
            set
            {
                _isNotFull = value;
                ProprieteModifiee();
            }
        }

        private string _lobbyName;
        public string LobbyName
        {
            get { return _lobbyName; }
            set
            {
                if (_lobbyName == value) return;
                _lobbyName = value;
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

        private bool _isPrivate;
        public bool IsPrivate
        {
            get { return _isPrivate; }
            set
            {
                _isPrivate = value;
                ProprieteModifiee();
            }
        }

        private bool _isPasswordDialogOpen;
        public bool IsPasswordDialogOpen
        {
            get { return _isPasswordDialogOpen; }
            set
            {
                if (_isPasswordDialogOpen == value) return;
                _isPasswordDialogOpen = value;
                ProprieteModifiee();
            }
        }

        private bool _isLobbyJoined;
        public bool IsLobbyJoined
        {
            get { return _isLobbyJoined; }
            set { _isLobbyJoined = value; ProprieteModifiee(); }
        }

        #endregion

        #region Methods
        private void OnCollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            ActualSize = _players.Count;
            if (ActualSize == Size)
                IsNotFull = false;
        }

        protected virtual void ProprieteModifiee([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private async void joinPublicLobby()
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.Add("isPrivate", _lobby.isPrivate);
            values.lobbyName = _lobby.lobbyName;
            values.password = "";
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if ((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                Dictionary<string, string> data = new Dictionary<string, string>();
                data.Add("lobbyName", _lobby.lobbyName);
                data.Add("mode", _lobby.mode);
                Mediator.Notify("GoToLobbyScreen", data);
            }
            IsLobbyJoined = true;
        }
        private async void joinPrivateLobby()
        {
            string errorMessage = "Wrong password, try again.";
            try
            {
                string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
                dynamic values = new JObject();
                values.username = ServerService.instance.username;
                values.Add("isPrivate", true);
                values.lobbyName = _lobby.lobbyName;
                values.password = Password.Password;
                var content = JsonConvert.SerializeObject(values);
                var buffer = System.Text.Encoding.UTF8.GetBytes(content);
                var byteContent = new ByteArrayContent(buffer);
                byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
                var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
                if ((int)response.StatusCode == Constants.SUCCESS_CODE)
                {
                    Dictionary<string, string> data = new Dictionary<string, string>();
                    data.Add("lobbyName", _lobby.lobbyName);
                    data.Add("mode", _lobby.mode);
                    Mediator.Notify("GoToLobbyScreen", data);
                }
                else
                    ShowMessageBox(errorMessage);
            }
            catch (Exception)
            {
                ShowMessageBox(errorMessage);
            }
            finally
            {
                IsLobbyJoined = true;
            }
            
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

        private ICommand _joinLobbyCommand;
        public ICommand JoinLobbyCommand
        {
            get
            {
                return _joinLobbyCommand ?? (_joinLobbyCommand = new RelayCommand(x =>
                {
                    IsLobbyJoined = false;
                    if (IsPrivate)
                    {
                        DialogContent = new LobbyPasswordControl();
                        IsPasswordDialogOpen = true;
                    }
                    else
                    {
                        joinPublicLobby();
                    }
                }));
            }
        }

        private ICommand _sendPasswordCommand;
        public ICommand SendPasswordCommand
        {
            get
            {
                return _sendPasswordCommand ?? (_sendPasswordCommand = new RelayCommand(x =>
                {
                    joinPrivateLobby();
                }));
            }
        }

        private ICommand _cancelPasswordCommand;
        public ICommand CancelPasswordCommand
        {
            get
            {
                return _cancelPasswordCommand ?? (_cancelPasswordCommand = new RelayCommand(x =>
                {
                    this.IsPasswordDialogOpen = false;
                }));
            }
        }

        #endregion
    }
}
