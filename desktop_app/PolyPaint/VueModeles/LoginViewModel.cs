using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class LoginViewModel : BaseViewModel, IPageViewModel, IDisposable
    {
        private ICommand    _login;
        private ICommand    _goToRegister;
        private bool        _isButtonEnabled;
        private bool        _loginIsRunning;
        private string      _username;

        public LoginViewModel()
        {
            _loginIsRunning = false;
            fetchProfile();
        }

        #region Public Attributes
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
                ProprieteModifiee();
            }
        }
        #endregion

        #region Methods
        private void ReceiveMessage(JObject jsonMessage)
        {
            var status = jsonMessage["status"].ToObject<int>();
            var message = jsonMessage["message"].ToObject<string>();
            if (status == 200)
            {
                ServerService.instance.username = _username;
                fetchProfile();
                Mediator.Notify("GoToHomeScreen", "");
                Dispose();

            }
            else
                App.Current.Dispatcher.Invoke(delegate
                {
                    ShowMessageBox(message);
                });
        }

        private async void fetchProfile()
        {
            try
            {
                var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.USER_INFO_PATH + ServerService.instance.username);
                if (response.IsSuccessStatusCode)
                {
                    string responseString = await response.Content.ReadAsStringAsync();
                    var data = JsonConvert.DeserializeObject<User>(responseString);
                    ServerService.instance.user = data;
                }
            }
            catch (Exception)
            {
                ShowMessageBox("Failed to connect!");
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

        public override void Dispose()
        {
            ServerService.instance.socket.Off(Constants.LOGGING_EVENT);
        }
        
        private void ShowMessageBox(string message)
        {
            App.Current.Dispatcher.Invoke(delegate
            {
                MessageBoxDisplayer.ShowMessageBox(message);
            });
        }

        #endregion

        #region Commands
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
                        IsButtonEnabled = false;
                        JObject res = await LoginRequestAsync(_username, Password.Password);

                        if (res.ContainsKey("status"))
                        {
                            if (res.GetValue("status").ToObject<int>() == Constants.SUCCESS_CODE)
                            {
                                ServerService.instance.socket.On(Constants.LOGGING_EVENT, data => ReceiveMessage((JObject)data));
                                ServerService.instance.socket.Emit(Constants.LOGIN_EVENT, _username);
                            }
                            else
                                ShowMessageBox(res.GetValue("message").ToString());
                        } 
                    } catch
                    {

                        ShowMessageBox("Error while logging into server.");
                    }
                    finally
                    {
                        _loginIsRunning = false;
                        IsButtonEnabled = true;
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

        #endregion

    }
}
