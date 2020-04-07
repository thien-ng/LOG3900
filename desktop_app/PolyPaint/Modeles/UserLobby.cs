using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class UserLobby
    {
        public string username { get; set; }
        public bool isMyself { get; set; }

        public UserLobby(string username, bool isMyself)
        {
            this.username = username;
            this.isMyself = isMyself;
        }
    }
}
