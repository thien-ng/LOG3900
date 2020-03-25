using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Controls;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using System.IO;

namespace PolyPaint.VueModeles
{
    class GamelistViewModel : BaseViewModel
    {
        public GamelistViewModel()
        {

            _gameCards = new ObservableCollection<GameCard>();
            getLobbies();
            Numbers = new ObservableCollection<int> { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            SelectedMode = "FFA";
            IsPrivate = false;
        }

        #region Public Attributes
        public ObservableCollection<int> Numbers { get; }

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
            set { _selectedMode = value; ProprieteModifiee(); getLobbies(); }
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

        private async void getLobbies()
        {
            if (_selectedMode == "FFA" || _selectedMode == "SOLO" || _selectedMode == "COOP")
            {
                GameCards.Clear();
                ObservableCollection<Lobby> lobbies = new ObservableCollection<Lobby>();
                var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.GET_ACTIVE_LOBBY_PATH + "/" + _selectedMode);

                StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
                String responseData = streamReader.ReadToEnd();
                var myData = JsonConvert.DeserializeObject<List<Lobby>>(responseData);
                foreach (var item in myData)
                {
                    App.Current.Dispatcher.Invoke(delegate
                    {
                        lobbies.Add(item);
                    });
                }
                foreach (var item in lobbies)
                {
                    Console.WriteLine("get lobbies" + item.isPrivate);
                    GameCard gameCard = new GameCard(item);
                    GameCards.Add(gameCard);
                }
            }
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
                App.Current.Dispatcher.Invoke(delegate
                {
                    getLobbies();
                });
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
