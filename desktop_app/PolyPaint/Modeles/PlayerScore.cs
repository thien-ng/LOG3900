
namespace PolyPaint.Modeles
{
    class PlayerScore
    {
        public string _username { get; set; }
        public int _score { get; set; }

        public PlayerScore(string username, int score) 
        {
            _username = username;
            _score = score;
        }
    }
}
