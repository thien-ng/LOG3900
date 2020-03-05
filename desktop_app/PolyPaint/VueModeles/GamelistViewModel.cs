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

            //Mediator.Subscribe("addGame", addGame);
            _gameCards = new ObservableCollection<GameCard>();
            getGameCards();
            Mode = new ObservableCollection<string> { "Free for all", "Sprint coop", "Sprint solo" };

        }

        public ObservableCollection<string> Mode { get; }

        private async void getGameCards()
        {
            ObservableCollection<GameCard> gamecards = new ObservableCollection<GameCard>();
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.GAMECARDS_PATH);

            Console.WriteLine(response.Content);
            StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
            String responseData = streamReader.ReadToEnd();
            Console.WriteLine(responseData);
            var myData = JsonConvert.DeserializeObject<List<GameCard>>(responseData);
            Console.WriteLine(myData);
            foreach (var item in myData)
            {
                App.Current.Dispatcher.Invoke(delegate
                {

                    gamecards.Add(item);
                });
            }
            GameCards = gamecards;
            //JArray responseJson = JArray.Parse(await response.Content.ReadAsStringAsync());

            //foreach (var item in responseJson)
                //App.Current.Dispatcher.Invoke(delegate
               // {
              //  GameCards.Add(item.ToObject<GameCard>());
             //   });
        }
        private ObservableCollection<GameCard> _gameCards;
        public ObservableCollection<GameCard> GameCards
        {
            get { return _gameCards; }
            set { _gameCards = value; ProprieteModifiee(); }
        }


        private ICommand _addGameCommand;
        public ICommand AddGameCommand
        {
            get
            {
                return _addGameCommand ?? (_addGameCommand = new RelayCommand(x =>
                {
                    DialogContent = new CreateGameControl();
                    IsCreateGameDialogOpen = true;
                }));
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

        private async void postCardRequest(string gamename, string selectedmode)
        {
            dynamic values = new JObject();
            values.gameName = gamename;
            values.solution = "solution";
            values.clues = "clues";
            values.mode = selectedmode;
            Console.WriteLine(values);
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.CARDSCREATOR_PATH, byteContent);
            Console.WriteLine(response.StatusCode);
            getGameCards();

        }


        private string _selectedMode;
        public string SelectedMode
        {
            get { return _selectedMode; }
            set { _selectedMode = value; ProprieteModifiee(); }
        }



        private void createGame()
        {
            postCardRequest(GameName, SelectedMode);
            

        }

        private ICommand _acceptCommand;
        public ICommand AcceptCommand
        {
            get
            {
                return _acceptCommand ?? (_acceptCommand = new RelayCommand(async x =>
                {
                    Console.WriteLine("hel");
                    await Task.Run(() => createGame());
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

    }
}
