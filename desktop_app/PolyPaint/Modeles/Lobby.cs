namespace PolyPaint.Modeles
{
    public class Lobby
    {
        string lobbyName { get; set; }
        string username { get; set; }
        bool isPrivate { get; set; }
        int size { get; set; }
        string password { get; set; }

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