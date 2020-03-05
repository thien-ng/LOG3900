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

        }

        public ObservableCollection<string> Mode;

        private async void getGameCards()
        {
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.GAMECARDS_PATH);

            Console.WriteLine(response.Content);
            StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
            String responseData = streamReader.ReadToEnd();
            Console.WriteLine(responseData);
            var myData = JsonConvert.DeserializeObject<List<GameCard>>(responseData);

            foreach (var item in myData)
            {
                GameCards.Add(item);
            }
            JArray responseJson = JArray.Parse(await response.Content.ReadAsStringAsync());

            foreach (var item in responseJson)
                GameCards.Add(item.ToObject<GameCard>());
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
                    GameCard card = new GameCard("the game", "the mode");
                    postCardRequest(card);
                    _gameCards.Add(card);
                }));
            }
        }

        private async void postCardRequest(GameCard card)
        {
            dynamic values = new JObject();
            values.gameName = card.GameName;
            values.solution = "";
            values.clues = "";
            values.mode = card.Mode;
            Console.WriteLine(values);
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + "/" + Constants.CARDSCREATOR_PATH, byteContent);
        }
       

        private string _newGameString;
        public string NewGameString
        {
            get { return _newGameString; }
            set
            {
                if (_newGameString == value) return;
                _newGameString = value;
                ProprieteModifiee();
            }
        }

        private void addGame()
        {
            throw new NotImplementedException();
        }

        

        private void createGame()
        {
            throw new NotImplementedException();
        }

        private ICommand test;
        public ICommand TEST
        {
            get
            {
                return test ?? (test = new RelayCommand(x => { Mediator.Notify("GoToDrawScreen", ""); }));
            }
        }
    }
}
