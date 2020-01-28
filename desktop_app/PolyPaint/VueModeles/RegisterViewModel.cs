using PolyPaint.Utilitaires;
using System;
using System.Windows.Input;
using System.Windows.Controls;
using System.Collections.Generic;
using System.Net.Http;

namespace PolyPaint.VueModeles
{
    class RegisterViewModel : VueModele, IPageViewModel
    {
        private ICommand _goToLogin;
        private ICommand _register;
        private string _username;

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

        public ICommand Register
        {
            get
            {
                return _register ?? (_register = new RelayCommand(async x =>
                {
                    await postRequestAsync(_username,Password.Password);
                    Console.WriteLine("User: " + this._username);
                    Console.WriteLine("pswd: " + this.Password.Password);
                    Console.WriteLine("cfrm: " + this.PasswordConfirm.Password);
                }));
            }
        }

        public async System.Threading.Tasks.Task postRequestAsync(string username, string password)
        {
            var values = new Dictionary<string, string>
                {
                    { "username", username },
                    { "password", password }
                };

            var content = new FormUrlEncodedContent(values);

            var response = await VueModele.client.PostAsync("http://72.53.102.93:3000/account/register", content);

            var responseString = await response.Content.ReadAsStringAsync();
            Console.WriteLine(responseString);
        }
    }

}
