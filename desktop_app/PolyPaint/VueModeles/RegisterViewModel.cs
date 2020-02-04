﻿using PolyPaint.Utilitaires;
using System;
using System.Windows.Input;
using System.Windows.Controls;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using PolyPaint.Services;

namespace PolyPaint.VueModeles
{
    class RegisterViewModel : BaseViewModel, IPageViewModel
    {
        private ICommand _goToLogin;
        private ICommand _register;
        private string   _username;

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
                    JObject res = await RegisterRequestAsync(_username,Password.Password);

                    if (res.ContainsKey("status"))
                    {
                        if (res.GetValue("status").ToObject<int>() == Constants.SUCCESS_CODE)
                            Mediator.Notify("GoToLoginScreen", "");
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
