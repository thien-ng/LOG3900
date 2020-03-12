using Newtonsoft.Json;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.VueModeles
{
    class LobbyViewModel: BaseViewModel, IPageViewModel
    {
        public ObservableCollection<string> _usernames;
        public LobbyViewModel()
        {
            //fetchUsername();
        }

        private async void fetchUsername()
        {
            ObservableCollection<string> usernames = new ObservableCollection<string>();
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.GAMECARDS_PATH);//TODO

            StreamReader streamReader = new StreamReader(await response.Content.ReadAsStreamAsync());
            String responseData = streamReader.ReadToEnd();
            var myData = JsonConvert.DeserializeObject<List<String>>(responseData);
            foreach (var item in myData)
            {
                App.Current.Dispatcher.Invoke(delegate
                {
                    _usernames.Add(item);
                });
            }
            //GameCards = gamecards; TODO
        }
    }
}
