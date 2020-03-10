using PolyPaint.Utilitaires;
using System.Windows.Controls;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class DessinViewModel : VueModele, IPageViewModel
    {
        private ICommand _goToLogin;

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
