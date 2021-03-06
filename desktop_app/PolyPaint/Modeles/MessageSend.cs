﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class MessageSend
    {
        public string username   { get; set; }
        public string channel_id { get; set; }
        public string content    { get; set; }

        public MessageSend(string username, string content, string channel_id)
        {
            this.username = username;
            this.channel_id = channel_id; 
            this.content = content;
        }
    }
}
