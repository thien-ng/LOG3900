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

namespace PolyPaint.VueModeles
{
    class GamelistViewModel : BaseViewModel
    {
        public GamelistViewModel()
        {

            _gameCards = new ObservableCollection<GameCard>();
            //_ = getGameCards();

        }

        private async Task getGameCards()
        {
            if (_gameCards.Count > 0)
            {
                App.Current.Dispatcher.Invoke(delegate
                {
                    _gameCards.Clear();
                });
            }

            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.GAMECARDS_PATH);

            StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
            string responseData = streamReader.ReadToEnd();
            var myData = JsonConvert.DeserializeObject<List<GameCard>>(responseData);
            foreach (var item in myData)
            {
                Console.WriteLine(item.GameName);
                App.Current.Dispatcher.Invoke(delegate
                {
                    _gameCards.Add(item);
                });
            }
        }

        private ObservableCollection<GameCard> _gameCards;
        public ObservableCollection<GameCard> GameCards
        {
            get { return _gameCards; }
            set { _gameCards = value; ProprieteModifiee(); }
        }
    }
}
