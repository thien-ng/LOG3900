
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System.Net.Http;
using System.Net.Http.Headers;

namespace PolyPaint.VueModeles
{
    class CreateGameViewModel: BaseViewModel
    {
        
        private async void postCardRequest(string gamename, string selectedmode)
        {
            dynamic values = new JObject();
            values.gameName = gamename;
            values.solution = "solution";
            values.clues = "clues";
            values.mode = selectedmode;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.CARDSCREATOR_PATH, byteContent);
            getGameCards();

        }
    }
}
