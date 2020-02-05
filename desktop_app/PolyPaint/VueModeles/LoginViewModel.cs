using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    // TODO unit test
    class LoginViewModel : BaseViewModel, IPageViewModel
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

                IsButtonEnabled = condition;
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

                                ServerService.instance.username = _username;
                                ServerService.instance.socket.Emit(Constants.LOGIN_EVENT, _username);

                                Mediator.Notify("GoToChatScreen", "");
                            }
                            else
                            {
                                MessageBox.Show(res.GetValue("message").ToString());
                            }
                        } 
                    }
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

            try
            {
                var response = await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.LOGIN_PATH, content);
                var responseString = await response.Content.ReadAsStringAsync();
                return JObject.Parse(responseString);
            }
            catch
            {
                return JObject.Parse("{ status: '500', content: 'Could not connect to server' }");
            }
        }

        public void OnPasswordPropertyChanged()
        {
            bool condition = _username != null &&
                             _username.Length >= Constants.USR_MIN_LENGTH &&
                             Password.SecurePassword.Length >= Constants.PWD_MIN_LENGTH;


            IsButtonEnabled = condition;
        }

    }
}
