using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class MessageReception: MessageSend
    {
        public string time { get; set; }

        public MessageReception(string username, string content, int channel_id, string time): base(username, content, channel_id)
        {
            this.time = time;
        }
    }
}
