using PolyPaint.Services;
using System.Windows.Input;
using PolyPaint.Utilitaires;
using Quobject.SocketIoClientDotNet.Client;
using System.Text.RegularExpressions;
using System;
using System.Net;

namespace PolyPaint.VueModeles
{
    class ChooseIPViewModel: BaseViewModel, IPageViewModel
    {

        private Regex IP_REGEX = new Regex(@"(^https?://){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$");
        private Regex URL_REGEX = new Regex(@"(^https?://.*)$");


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

        private bool ping(string ip)
        {
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(ip + "/date");
            request.AllowAutoRedirect = false;
            request.Method = "HEAD";
            
            try
            {
                HttpWebResponse response = (HttpWebResponse)request.GetResponse();
                if (response.StatusCode == HttpStatusCode.OK)
                    return true;
                else
                    return false;
            }
            catch (WebException)
            {
                return false;
            }
        }

        private ICommand _submitIp;
        public ICommand SubmitIp
        {
            get
            {
                return _submitIp ?? (_submitIp = new RelayCommand(x=>
                {
                    IsButtonEnabled = false;

                    var serverPath = x.ToString();
                    bool isPathValid = IP_REGEX.Match(serverPath).Success || URL_REGEX.Match(serverPath).Success;

                    if (isPathValid && ping(serverPath))
                    {
                        Constants.SERVER_PATH = serverPath;

                        Socket socket = IO.Socket(serverPath);
                        socket.On(Socket.EVENT_CONNECT, () =>
                        {
                            ServerService.instance.socket = socket;
                            Mediator.Notify("GoToLoginScreen");
                        });
                    } else
                    {
                        IsButtonEnabled = true;
                        App.Current.Dispatcher.Invoke(delegate
                        {
                            MessageBoxDisplayer.ShowMessageBox("Please enter a valid server path");
                        });
                    }
                }));
            }
        }
    }
}
