using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class MessageGameReception
    {
        public string username { get; set; }
        public bool isServer { get; set; }
        public string content { get; set; }

        public MessageGameReception(string username, string content, bool isServer)
        {
            this.username = username;
            this.content = content;
            this.isServer = isServer;
        }
    }
}

