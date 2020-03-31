namespace PolyPaint.Modeles
{
    class Game
    {
        public string date { get; set; }
        public string mode { get; set; }
        public string playerScore { get; set; }

        public Game(string date, string mode, PlayerScore[] players) 
        {
            this.date = date;
            this.mode = mode;
            foreach (PlayerScore player in players) 
            {
                playerScore += (player.username + ": " + player.score + ", ");
            }
        }
    }
}
