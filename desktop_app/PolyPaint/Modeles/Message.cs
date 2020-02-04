using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class Message
    {
        public string username   { get; set; }
        public int    channel_id { get; set; }
        public string content    { get; set; }

        public Message(string username, string content, int channel_id)
        {
            this.username = username;
            this.channel_id = channel_id; 
            this.content = content;
        }
    }
}
