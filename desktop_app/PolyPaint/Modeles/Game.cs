namespace PolyPaint.Modeles
{
    class Game
    {
        public string _date { get; set; }
        public PlayerScore[] _players;

        public Game(string date, PlayerScore[] players) 
        {
            _date = date;
            _players = players;
        }
    }
}
