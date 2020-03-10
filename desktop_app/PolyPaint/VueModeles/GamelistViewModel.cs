using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Controls;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Windows.Input;
using System.IO;
using MaterialDesignThemes.Wpf;

namespace PolyPaint.VueModeles
{
    class GamelistViewModel : BaseViewModel
    {
        public GamelistViewModel()
        {

            _gameCards = new ObservableCollection<GameCard>();
            getGameCards();
            Mode = new ObservableCollection<string> { "Free for all", "Sprint coop", "Sprint solo" };

        }

        public ObservableCollection<string> Mode { get; }

        private async void getGameCards()
        {
            ObservableCollection<GameCard> gamecards = new ObservableCollection<GameCard>();
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.GAMECARDS_PATH);

            StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
            string responseData = streamReader.ReadToEnd();
            var myData = JsonConvert.DeserializeObject<List<GameCard>>(responseData);
            foreach (var item in myData)
            {
                App.Current.Dispatcher.Invoke(delegate
                {

                    gamecards.Add(item);
                });
            }
            GameCards = gamecards;
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
                return _addGameCommand ?? (_addGameCommand = new RelayCommand(async x =>
                {
                    var view = new CreateGameControl { DataContext = new CreateGameViewModel() };

                    await DialogHost.Show(view, "RootDialog");
                }));
            }
        }
    }
}
