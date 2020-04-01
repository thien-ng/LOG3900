using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class Points
    {
        public string username { get; set; }
        public int points { get; set; }

        public Points(string username, int points)
        {
            this.username = username;
            this.points = points;
        }
    }
}
