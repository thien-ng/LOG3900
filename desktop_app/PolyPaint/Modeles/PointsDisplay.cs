using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class PointsDisplay
    {
        public string username { get; set; }
        public int points { get; set; }
        public int rank { get; set; }
        public string color { get; set; }

        public PointsDisplay(string username, int points, int rank)
        {
            this.username = username;
            this.points = points;
            this.rank = rank;
            switch (rank)
            {
                case 1:
                    color = "LightGreen";
                    break;

                case 2:
                    color = "Orange";
                    break;

                case 3:
                    color = "Red";
                    break;

                default:
                    color = "LightGray";
                    break;
            }
        }
    }
}
