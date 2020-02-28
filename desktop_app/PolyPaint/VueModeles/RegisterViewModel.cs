using PolyPaint.Utilitaires;
using System;
using System.Windows.Input;
using System.Windows.Controls;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using System.Windows;

namespace PolyPaint.VueModeles
{
    class RegisterViewModel : BaseViewModel, IPageViewModel
    {
        private ICommand _goToLogin;
        private ICommand _register;
        private string   _username;
        private string   _firstName;
        private string   _name;
        private bool     _registerIsRunning;

        public PasswordBox Password { private get; set; }
        public PasswordBox PasswordConfirm { private get; set; }

        public ICommand GoToLogin
        {
            get
            {
                return _goToLogin ?? (_goToLogin = new RelayCommand(x =>
                {
                    Mediator.Notify("GoToLoginScreen", "");
                }));
            }
        }

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
        public string FirstName
        {
            get { return _firstName; }
            set
            {
                if (value != _firstName)
                {
                    _firstName = value;
                    ProprieteModifiee("FirstName");
                }
            }
        }
        public string Name
        {
            get { return _username; }
            set
            {
                if (value != _name)
                {
                    _name = value;
                    ProprieteModifiee("Name");
                }
            }
        }

        private void ReceiveMessage(JObject jsonMessage)
        {
            var status = jsonMessage["status"].ToObject<int>();
            var message = jsonMessage["message"].ToObject<string>();
            if (status == 200)
            {
                Mediator.Notify("GoToChatScreen", "");
                MessageBox.Show(message);
            }
            else
            {
                MessageBox.Show(message);
            }
        }

        public ICommand Register
        {
            get
            {
                return _register ?? (_register = new RelayCommand(async x =>
                {
                    if (_registerIsRunning)
                        return;

                    if (_username == null || Password.SecurePassword.Length == 0)
                    {
                        MessageBox.Show("Please fill every parameter");
                        return;
                    }

                    try
                    {
                        _registerIsRunning = true;

                        JObject res = await RegisterRequestAsync(_username,Password.Password);

                        if (res.ContainsKey("status"))
                        {
                            if (res.GetValue("status").ToObject<int>() == Constants.SUCCESS_CODE)
                            {
                                ServerService.instance.username = _username;
                                ServerService.instance.socket.On(Constants.LOGGING_EVENT, data => ReceiveMessage((JObject)data));
                                ServerService.instance.socket.Emit(Constants.LOGIN_EVENT, _username);
                            }
                            else
                                MessageBox.Show(res.GetValue("message").ToString());
                        }
                    }
                    finally
                    {
                        _registerIsRunning = false;
                    }
                }));
            }
        }

        public async Task<JObject> RegisterRequestAsync(string username, string password)
        {
            var values = new Dictionary<string, string>
                {
                    { "username", username },
                    { "password", password }
                };

            var content = new FormUrlEncodedContent(values);

            var response = await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.REGISTER_PATH, content);

            var responseString = await response.Content.ReadAsStringAsync();

            return JObject.Parse(responseString);
        }
    }

}
