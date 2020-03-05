using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows.Input;

namespace PolyPaint.Modeles
{
    public class GameCard: INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        public GameCard(string gameName,string mode)
        {
            _visibilityPrivate = "Hidden";
            _gameName = gameName;
            _mode = mode;
            Numbers = new ObservableCollection<int> { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            _gameLobbies = new ObservableCollection<Lobby>();

        }

        public ObservableCollection<int> Numbers { get; }

        private int _gameID;
        public string GameID { get; set; }

        private string _mode;
        public string Mode { get { return _mode; } }
        private string _gameName;
        public string GameName
        {
            get { return _gameName; }
            set { _gameName = value; }
        }

        private ObservableCollection<Lobby> _gameLobbies;
        public ObservableCollection<Lobby> GameLobbies
        {
            get { return _gameLobbies; }
            set { _gameLobbies = value; ProprieteModifiee(nameof(GameLobbies)); }
        }
        protected virtual void ProprieteModifiee([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private async void createLobby()
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.Add("private", _isPrivate);
            values.lobbyName = _lobbyName;
            values.size = _selectedSize;
            values.password = _password;
            Console.WriteLine(values);
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            Console.WriteLine(response);
            if((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                Lobby lobby = new Lobby(_lobbyName, ServerService.instance.username, _isPrivate, int.Parse(_selectedSize), _password);
                App.Current.Dispatcher.Invoke(delegate
                {
                    GameLobbies.Add(lobby);
                });
                LobbyName = "";
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
            set { _selectedSize = value; ProprieteModifiee(); Console.WriteLine(_selectedSize); }
        }

        private ICommand _acceptCommand;
        public ICommand AcceptCommand
        {
            get
            {
                return _acceptCommand ?? (_acceptCommand = new RelayCommand(async x =>
                {
                    await Task.Run(() => createLobby());
                    IsCreateGameDialogOpen = false;
                }));
            }
        }
        private ICommand _addLobbyCommand;
        public ICommand AddLobbyCommand
        {
            get
            {
                return _addLobbyCommand ?? (_addLobbyCommand = new RelayCommand(x =>
                {
                    DialogContent = new CreateLobbyControl();
                    IsCreateGameDialogOpen = true;
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
                    LobbyName = "";
                    IsCreateGameDialogOpen = false;
                }));
            }
        }
        private bool _isCreateGameDialogOpen;
        public bool IsCreateGameDialogOpen
        {
            get { return _isCreateGameDialogOpen; }
            set
            {
                if (_isCreateGameDialogOpen == value) return;
                _isCreateGameDialogOpen = value;
                ProprieteModifiee();
            }
        }

        private ICommand _deleteCommand;
        public ICommand DeleteCommand
        {
            get
            {
                return _deleteCommand ?? (_deleteCommand = new RelayCommand(x =>
                {
                    LobbyName = "";
                    IsCreateGameDialogOpen = false;
                }));
            }
        }

    }

}
