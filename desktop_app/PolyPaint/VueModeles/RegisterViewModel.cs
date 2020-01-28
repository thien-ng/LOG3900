using PolyPaint.Utilitaires;
using PolyPaint.Modeles;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Security;
using System.Windows.Controls;

namespace PolyPaint.VueModeles
{
    class RegisterViewModel : VueModele, IPageViewModel
    {
        private ICommand _goToLogin;
        private ICommand _register;

        public PasswordBox SecurePassword { private get; set; }
        public PasswordBox SecurePasswordConfirm { private get; set; }

        public ICommand Register
        {
            get
            {
                return _register ?? (_register = new RelayCommand(x =>
                {

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

    }
}
