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
using Newtonsoft.Json;
using PolyPaint.Modeles;

namespace PolyPaint.VueModeles
{
    class RegisterViewModel : BaseViewModel, IPageViewModel, IDisposable
    {
        private ICommand _goToLogin;
        private ICommand _register;
        private string   _username;
        private string   _firstName;
        private string   _lastName;
        private bool     _registerIsRunning;
        public RegisterViewModel()
        {
            _username = "";
            _firstName = "";
            _lastName = "";
        }

        #region Public Attributes
        public PasswordBox Password { private get; set; }
        public PasswordBox PasswordConfirm { private get; set; }

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
        public string LastName
        {
            get { return _lastName; }
            set
            {
                if (value != _lastName)
                {
                    _lastName = value;
                    ProprieteModifiee("LastName");
                }
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
                ShowMessageBox(message);
        }

        private async void fetchProfile()
        {
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.USER_INFO_PATH + ServerService.instance.username);
            if (response.IsSuccessStatusCode)
            {
                string responseString = await response.Content.ReadAsStringAsync();
                var data = JsonConvert.DeserializeObject<User>(responseString);
                ServerService.instance.user = data;
            }
        }
        public async Task<JObject> RegisterRequestAsync(string username, string password, string firstName, string lastName)
        {
            var values = new Dictionary<string, string>
                {
                    { "username", username },
                    { "password", password },
                    { "firstName", firstName},
                    { "lastName", lastName}
                };

            var content = new FormUrlEncodedContent(values);

            var response = await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.REGISTER_PATH, content);

            var responseString = await response.Content.ReadAsStringAsync();

            fetchProfile();

            return JObject.Parse(responseString);
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

        public ICommand Register
        {
            get
            {
                return _register ?? (_register = new RelayCommand(async x =>
                {
                    try
                    {
                        if (_registerIsRunning)
                            return;

                        if (_username == null || Password.SecurePassword == null || _firstName == null || _lastName == null)
                        {
                            ShowMessageBox("Please fill every parameter");
                            return;
                        }

                        if (!Password.Password.Equals(PasswordConfirm.Password))
                        {
                            ShowMessageBox("Passwords doesnt match");
                            return;
                        }

                        _registerIsRunning = true;

                        JObject res = await RegisterRequestAsync(_username,Password.Password, _firstName, _lastName);

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
                    }
                    catch
                    {
                        ShowMessageBox("Error while logging into server");
                    }
                    finally
                    {
                        _registerIsRunning = false;
                    }
                }));
            }
        }

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
        #endregion

    }

}
