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

    }
}