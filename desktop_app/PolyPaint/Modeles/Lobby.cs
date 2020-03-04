namespace PolyPaint.Modeles
{
    public class Lobby
    {
        public string lobbyName { get; set; }
        public string username { get; set; }
        public bool isPrivate { get; set; }
        public int size { get; set; }
        public string password { get; set; }

        public Lobby(string lobbyName, string username, bool isPrivate, int size, string password)
        {
            this.lobbyName  = lobbyName;
            this.username   = username;
            this.isPrivate  = isPrivate;
            this.size       = size;
            this.password   = password;
        }

    }
}