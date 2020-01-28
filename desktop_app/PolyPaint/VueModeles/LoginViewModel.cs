using Newtonsoft.Json.Linq;
using PolyPaint.Utilitaires;
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
                            Mediator.Notify("GoToDrawScreen", "");
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

            var response = await client.PostAsync("http://72.53.102.93:3000/account/login", content);

            var responseString = await response.Content.ReadAsStringAsync();

            return JObject.Parse(responseString);
        }

    }
}
