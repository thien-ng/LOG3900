using PolyPaint.Services;
using System.Windows.Input;
using PolyPaint.Utilitaires;
using Quobject.SocketIoClientDotNet.Client;

namespace PolyPaint.VueModeles
{
    class ChooseIPViewModel: BaseViewModel
    {

        public ChooseIPViewModel()
        {
            _isButtonEnabled = true;
        }

        private bool _isButtonEnabled;
        public bool IsButtonEnabled
        {
            get { return _isButtonEnabled; }
            set { _isButtonEnabled = value; ProprieteModifiee(); }
        }

        private string _serverPath;
        public string ServerPath
        {
            get { return _serverPath; }
            set { _serverPath = value; ProprieteModifiee(); }
        }

        private ICommand _submitIp;
        public ICommand SubmitIp
        {
            get
            {
                return _submitIp ?? (_submitIp = new RelayCommand(x =>
                {
                    Constants.SERVER_PATH = _serverPath;

                    Socket socket = IO.Socket(_serverPath);
                    socket.On(Socket.EVENT_CONNECT, () =>
                    {
                        ServerService.instance.socket = socket;
                    });
                }));
            }
        }
    }
}
