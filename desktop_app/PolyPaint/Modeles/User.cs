namespace PolyPaint.Modeles
{
    class User
    {
        public string username;
        public string firstName;
        public string lastName;
        public Connection[] connections;
        public Stats stats;
        public Game[] games;

        public User(string username, string firstName, string lastName, Connection[] connections, Stats stats, Game[] games)
        {
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.connections = connections;
            this.stats = stats;
            this.games = games;
        }
    }
}
