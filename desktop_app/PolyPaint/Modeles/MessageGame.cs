using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    public class MessageGame
    {
        public string username { get; set; }
        public string sender { get; set; }
        public string content { get; set; }

        public MessageGame(string username, string content, string sender)
        {
            this.username = username;
            this.content = content;
            this.sender = sender;
        }
    }
}
