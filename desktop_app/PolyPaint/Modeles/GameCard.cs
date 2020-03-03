using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class GameCard
    {
        public string _gameName;
        public GameCard()
        {
            _gameName = "The card";
        }

        public string GameName
        {
            get { return _gameName; }
            set { _gameName = value; }
        }
    }
}
