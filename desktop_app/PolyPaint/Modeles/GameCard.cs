using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows.Input;

namespace PolyPaint.Modeles
{
    public class GameCard : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        public GameCard(Lobby lobby)
        {
            _visibilityPrivate = "Hidden";
            _players = new ObservableCollection<string>();
            foreach (var item in lobby.usernames)
            {
                _players.Add(item);
            }

            _lobby = lobby;
            _mode = lobby.mode;
            _lobbyName = lobby.lobbyName;
        }

        private Lobby _lobby;
        public Lobby Lobby
        {
            get { return _lobby; }
            set { _lobby = value; ProprieteModifiee(nameof(Lobby)); }
        }



        private string _mode;
        public string Mode { get { return _mode; } }

        private ObservableCollection<string> _players;
        public ObservableCollection<string> Players
        {
            get { return _players; }
            set { _players = value; ProprieteModifiee(nameof(Players)); }
        }
        protected virtual void ProprieteModifiee([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
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

        private string _password;
        public string Password
        {
            get { return _password; }
            set
            {
                if (_password == value) return;
                _password = value;
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
                _isPrivate = value; ProprieteModifiee();
                if (_isPrivate)
                    VisibilityPrivate = "Visible";
                else
                    VisibilityPrivate = "Hidden";
            }
        }



        private string _visibilityPrivate;
        public string VisibilityPrivate
        {
            get { return _visibilityPrivate; }
            set { _visibilityPrivate = value; ProprieteModifiee(); }
        }

        private string _selectedSize;
        public string SelectedSize
        {
            get { return _selectedSize; }
            set { _selectedSize = value; ProprieteModifiee(); }
        }
        private async void joinLobby()
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.Add("private", _lobby.isPrivate);
            values.lobbyName = _lobby.lobbyName;
            values.size = _lobby.size;
            values.password = _lobby.password;
            values.mode = _lobby.mode;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            Console.WriteLine(response);
            Console.WriteLine(byteContent);
            Console.WriteLine(response.StatusCode);
            if ((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                Mediator.Notify("GoToLobbyScreen", _lobby.lobbyName);
            }
        }

        private ICommand _joinLobbyCommand;
        public ICommand JoinLobbyCommand
        {
            get
            {
                return _joinLobbyCommand ?? (_joinLobbyCommand = new RelayCommand(x =>
                {
                    Console.WriteLine("joinlobby");
                    joinLobby();


                }));
            }
        }

    }
}
