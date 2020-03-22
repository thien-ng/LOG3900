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
        public string mode { get; set; }

        public Lobby(string lobbyName, string[] usernames, bool isPrivate, int size, string password, string mode)
        {
            this.lobbyName  = lobbyName;
            this.usernames  = usernames;
            this.isPrivate  = isPrivate;
            this.size       = size;
            this.password   = password;
            this.mode       = mode;
        }
    }
}