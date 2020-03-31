using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    public class MessageGame
    {
        public string username { get; set; }
        public bool isServer { get; set; }
        public string content { get; set; }

        public MessageGame(string username, string content, bool isServer)
        {
            this.username = username;
            this.content = content;
            this.isServer = isServer;
        }
    }
}
