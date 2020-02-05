using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    public class MessageChat
    {
        public string Username { get; set; }

        public string Message { get; set; }

        public bool SentByMe { get; set; }

        public string TimeStamp { get; set; }

        public MessageChat(string username, string message, bool sentByMe, string timeStamp)
        {
            Username = username;
            Message = message;
            SentByMe = sentByMe;
            TimeStamp = timeStamp;
        }
    }
}
