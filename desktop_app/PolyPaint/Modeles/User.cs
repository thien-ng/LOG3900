namespace PolyPaint.Modeles
{
    class User
    {
        public string _username;
        public string _firstName;
        public string _lastName;
        public Connection[] _connections;
        public Stats _stats;
        public Game[] _games;

        public User(string username, string firstname, string lastname, Connection[] connections, Stats stats, Game[] games)
        {
            _username = username;
            _firstName = firstname;
            _lastName = lastname;
            _connections = connections;
            _stats = stats;
            _games = games;
        }
    }
}
