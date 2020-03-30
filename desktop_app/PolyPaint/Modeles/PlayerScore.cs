
namespace PolyPaint.Modeles
{
    class PlayerScore
    {
        public string username { get; set; }
        public int score { get; set; }

        public PlayerScore(string username, int score) 
        {
            this.username = username;
            this.score = score;
        }
    }
}
