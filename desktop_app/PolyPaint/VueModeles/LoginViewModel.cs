using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using Quobject.SocketIoClientDotNet.Client;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class LoginViewModel : VueModele, IPageViewModel
    {
        private ICommand    _login;
        private ICommand    _goToRegister;
        private bool        _isButtonEnabled;
        private bool        _loginIsRunning;
        private string      _username;

        public LoginViewModel()
        {
            _loginIsRunning = false;
        }
        
        public PasswordBox Password { private get; set; }

        public string Username
        {
            get { return _username; }
            set
            {
                if (value != _username)
                    _username = value;

                bool condition = Password != null &&
                                 value.Length >= Constants.USR_MIN_LENGTH && 
                                 Password.SecurePassword.Length >= Constants.PWD_MIN_LENGTH;

                if (condition)
                    IsButtonEnabled = true;
                else
                    IsButtonEnabled = false;
            }
        }

        public bool IsButtonEnabled
        {
            get { return _isButtonEnabled; }
            set
            {
                _isButtonEnabled = value;
                ProprieteModifiee(nameof(IsButtonEnabled));
            }
        }

        public ICommand Login
        {
            get
            {
                return _login ?? (_login = new RelayCommand(async x =>
                {
                    if (_loginIsRunning)
                        return;
                    try
                    {
                        _loginIsRunning = true;

                        JObject res = await LoginRequestAsync(_username, Password.Password);

                        if (res.ContainsKey("status"))
                        {
                            if (res.GetValue("status").ToObject<int>() == Constants.SUCCESS_CODE)
                            {
                                Socket socket = IO.Socket(Constants.SERVER_PATH);
                                socket.On(Socket.EVENT_CONNECT, () =>
                                {
                                    Console.WriteLine("connect");
                                    ServerService.instance.socket = socket;
                                });

                                Mediator.Notify("GoToChatScreen", "");
                            }
                        }

                    } catch { }
                    finally
                    {
                        _loginIsRunning = false;
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

        private async Task<JObject> LoginRequestAsync(string username, string password)
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

        public void OnPasswordPropertyChanged()
        {
            bool condition = _username != null &&
                             _username.Length >= Constants.USR_MIN_LENGTH &&
                             Password.SecurePassword.Length >= Constants.PWD_MIN_LENGTH;

            if (condition)
                IsButtonEnabled = true;
            else
                IsButtonEnabled = false;
        }

    }
}
