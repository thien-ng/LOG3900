using Newtonsoft.Json;
using PolyPaint.Modeles;
using PolyPaint.Controls;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Windows.Input;
using System.IO;
using MaterialDesignThemes.Wpf;
using System.Threading.Tasks;
using System;
using Newtonsoft.Json.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Linq;

namespace PolyPaint.VueModeles
{
    class GamelistViewModel : BaseViewModel
    {
        public GamelistViewModel()
        {
            _gameCards = new ObservableCollection<GameCard>();
            _numbers = new ObservableCollection<int> { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            SelectedMode = "FFA";
            IsPrivate = false;
            ServerService.instance.socket.On("lobby-notif", data => processLobbyNotif((JObject)data));
        }

        #region Public Attributes
        private ObservableCollection<int> _numbers;
        public ObservableCollection<int> Numbers
        {
            get { return _numbers; }
            set { _numbers = value; ProprieteModifiee(); }
        }

        private ObservableCollection<GameCard> _gameCards;
        public ObservableCollection<GameCard> GameCards
        {
            get { return _gameCards; }
            set { _gameCards = value; ProprieteModifiee(); }
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


        private string _selectedMode;
        public string SelectedMode
        {
            get { return _selectedMode; }
            set
            {
                if (_selectedMode == value)
                    return;
                _selectedMode = value;
                ProprieteModifiee();
                getLobbies();
                Numbers.Clear();
                switch (_selectedMode)
                {
                    case Constants.MODE_FFA:
                        fillArray(2, 9);
                        break;

                    case Constants.MODE_COOP:
                        fillArray(3, 5);
                        break;

                    case Constants.MODE_SOLO:
                        fillArray(2, 2);
                        break;

                    default:
                        fillArray(1, 9);
                        break;
                }
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

        private string _selectedSize;
        public string SelectedSize
        {
            get { return _selectedSize; }
            set { _selectedSize = value; ProprieteModifiee(); }
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



        private string _visibilityPrivate;
        public string VisibilityPrivate
        {
            get { return _visibilityPrivate; }
            set { _visibilityPrivate = value; ProprieteModifiee(); }
        }

        private string _gameName;
        public string GameName
        {
            get { return _gameName; }
            set
            {
                if (_gameName == value) return;
                _gameName = value;
                ProprieteModifiee();
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

        #endregion

        #region Methods

        private void processLobbyNotif(JObject data)
        {
            App.Current.Dispatcher.Invoke(delegate
            { 
                if ((data.GetValue("type").ToString() == "create" && data.GetValue("mode").ToString() == _selectedMode) || data.GetValue("type").ToString() == "delete")
                    getLobbies();
                if(((string)data.GetValue("type") == "join"))
                {
                    GameCards.SingleOrDefault(i => i.LobbyName == (string)data.GetValue("lobbyName")).Players.Add((string)data.GetValue("username"));
                }
                if (((string)data.GetValue("type") == "leave"))
                {
                    GameCards.SingleOrDefault(i => i.LobbyName == (string)data.GetValue("lobbyName")).Players.Remove((string)data.GetValue("username"));
                }
            });

        }

        private void fillArray(int min, int max) 
        {
            for (int i = min; i <= max; i++)
            {
                Numbers.Add(i);
            }
        }
        private async void getLobbies()
        {
            await App.Current.Dispatcher.Invoke(async delegate
             {
                 bool isExistingSelectedMode = (_selectedMode == Constants.MODE_FFA || _selectedMode == Constants.MODE_SOLO || _selectedMode == Constants.MODE_COOP);
                 if (isExistingSelectedMode)
                 {
                     GameCards.Clear();
                     var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.GET_ACTIVE_LOBBY_PATH + "/" + _selectedMode);

                     StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
                     String responseData = streamReader.ReadToEnd();
                     var myData = JsonConvert.DeserializeObject<List<Lobby>>(responseData);
                     foreach (var item in myData)
                     {
                             GameCard gameCard = new GameCard(item);
                             GameCards.Add(gameCard);
                     }
                 }
             });

        }
        private async void createLobby()
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.Add("isPrivate", _isPrivate);
            values.lobbyName = _lobbyName;
            values.size = _selectedSize;
            values.password = _password;
            values.mode = _selectedMode;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if ((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                Mediator.Notify("GoToLobbyScreen", _lobbyName);
                LobbyName = "";
            }
        }

        #endregion

        #region Command

        private ICommand _addGameCommand;
        public ICommand AddGameCommand
        {
            get
            {
                return _addGameCommand ?? (_addGameCommand = new RelayCommand(x =>
                {
                    DialogContent = new CreateLobbyControl();
                    IsCreateGameDialogOpen = true;
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
                    await Task.Run(() => createLobby());
                    IsCreateGameDialogOpen = false;
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
                    GameName = "";
                    IsCreateGameDialogOpen = false;
                }));
            }
        }

        private ICommand _modeFFA;
        public ICommand ModeFFA
        {
            get
            {
                return _modeFFA ?? (_modeFFA = new RelayCommand(x =>
                {
                    SelectedMode = "FFA";
                }));
            }
        }

        private ICommand _modeSolo;
        public ICommand ModeSolo
        {
            get
            {
                return _modeSolo ?? (_modeSolo = new RelayCommand(x =>
                {
                    SelectedMode = "SOLO";
                }));
            }
        }

        private ICommand _modeCoop;
        public ICommand ModeCoop
        {
            get
            {
                return _modeCoop ?? (_modeCoop = new RelayCommand(x =>
                {
                    SelectedMode = "COOP";
                }));
            }
        }
        #endregion
    }
}
