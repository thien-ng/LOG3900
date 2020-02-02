using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using PropertyChanged;
using Quobject.SocketIoClientDotNet.Client;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class LoginViewModel : VueModele, IPageViewModel
    {
        private ICommand _login;
        private ICommand _goToRegister;
        private string   _username;
        
        public PasswordBox Password { private get; set; }

        public string Username
        {
            get { return _username; }
            set
            {
                if (value != _username)
                {
                    _username = value;
                    ProprieteModifiee("Username");
                }
            }
        }

        public ICommand Login
        {
            get
            {
                return _login ?? (_login = new RelayCommand(async x =>
                {
                    JObject res = await LoginRequestAsync(_username, Password.Password);

                    if (res.ContainsKey("status"))
                    {
                        if (res.GetValue("status").ToString() == "200")
                        {
                            Socket socket = IO.Socket(Constants.SERVER_PATH);
                            socket.On(Socket.EVENT_CONNECT, () =>
                            {
                                Console.WriteLine(Socket.EVENT_CONNECT);
                                ServerService.instance.socket = socket;
                            });

                            Mediator.Notify("GoToChatScreen", "");
                        }
                    }
                }));
            }
        }

        public ICommand GoToRegister
        {
            get
            {
                return _goToRegister ?? (_goToRegister = new RelayCommand(x =>
                {
                    Mediator.Notify("GoToRegisterScreen", "");
                }));
            }
        }

        public async Task<JObject> LoginRequestAsync(string username, string password)
        {
            var values = new Dictionary<string, string>
                {
                    { "username", username },
                    { "password", password }
                };

            var content = new FormUrlEncodedContent(values);

            var response = await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.LOGIN_PATH, content);

            var responseString = await response.Content.ReadAsStringAsync();

            return JObject.Parse(responseString);
        }

    }
}
