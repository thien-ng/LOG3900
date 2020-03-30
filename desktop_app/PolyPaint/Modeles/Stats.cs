namespace PolyPaint.Modeles
{
    class Stats
    {
        public int totalGame { get; set; }
        public int bestSoloScore { get; set; }
        public int totalPlayTime { get; set; }
        public string winRate { get; set; }
        public string avgGameTime { get; set; }

        public Stats(int totalGame, int bestScore, int totalPlayTime, string winRate, string avgGameTime) 
        {
            this.totalGame = totalGame;
            this.bestSoloScore = bestScore;
            this.totalPlayTime = totalPlayTime;
            this.winRate = winRate;
            this.avgGameTime = avgGameTime;
        }
    }
}
