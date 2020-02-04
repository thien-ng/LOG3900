using Quobject.SocketIoClientDotNet.Client;
using System.Net.Http;

namespace PolyPaint.Services
{
    class ServerService
    {
        private static ServerService _instance;

        ServerService()
        {
            client = new HttpClient();
        }

        public static ServerService instance
        {
            get
            {
                if (_instance == null)
                    _instance = new ServerService();
                return _instance;
            }
        }

        public HttpClient client { get; }
        public Socket socket { get; set; }
        public string username { get; set; }
    }
}
