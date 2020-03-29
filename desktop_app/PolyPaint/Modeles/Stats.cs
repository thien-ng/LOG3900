namespace PolyPaint.Modeles
{
    class Stats
    {
        public int _totalGame { get; set; }
        public int _bestSoloScore { get; set; }
        public int _totalPlayTime { get; set; }
        public string _winRate { get; set; }
        public string _avgGameTime { get; set; }

        public Stats(int totalGame, int bestScore, int totalPlayTime, string winRate, string avgGameTime) 
        {
            _totalGame = totalGame;
            _bestSoloScore = bestScore;
            _totalPlayTime = totalPlayTime;
            _winRate = winRate;
            _avgGameTime = avgGameTime;
        }
    }
}
