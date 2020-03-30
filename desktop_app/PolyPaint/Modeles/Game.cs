namespace PolyPaint.Modeles
{
    class Game
    {
        public string _date { get; set; }
        public string _mode { get; set; }
        public PlayerScore[] _players;

        public Game(string date, string mode, PlayerScore[] players) 
        {
            _date = date;
            _mode = mode;
            _players = players;
        }
    }
}
