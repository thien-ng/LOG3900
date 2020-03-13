using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Windows.Input;

namespace PolyPaint.Modeles
{
    public class Lobby
    {
        public string lobbyName { get; set; }
        public string[] usernames { get; set; }
        public bool isPrivate { get; set; }
        public int size { get; set; }
        public string password { get; set; }
        public string gameID { get; set; }

        public Lobby(string lobbyName, string[] usernames, bool isPrivate, int size, string password, string gameID)
        {
            this.lobbyName  = lobbyName;
            this.usernames  = usernames;
            this.isPrivate  = isPrivate;
            this.size       = size;
            this.password   = password;
            this.gameID     = gameID;
        }

        private async void joinLobby()
        {
            string requestPath = Constants.SERVER_PATH + Constants.GAME_JOIN_PATH;
            dynamic values = new JObject();
            values.username = ServerService.instance.username;
            values.Add("private", this.isPrivate);
            values.lobbyName = this.lobbyName;
            values.password = this.password;
            var content = JsonConvert.SerializeObject(values);
            var buffer = System.Text.Encoding.UTF8.GetBytes(content);
            var byteContent = new ByteArrayContent(buffer);
            byteContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
            var response = await ServerService.instance.client.PostAsync(requestPath, byteContent);
            if ((int)response.StatusCode == Constants.SUCCESS_CODE)
            {
                Mediator.Notify("GoToDrawScreen");
            }
        }

        private ICommand _joinLobbyCommand;
        public ICommand JoinLobbyCommand
        {
            get
            {
                return _joinLobbyCommand ?? (_joinLobbyCommand = new RelayCommand(x =>
                {
                    joinLobby();
                    Mediator.Notify("joinLobby", lobbyName);

                }));
            }
        }

    }
}