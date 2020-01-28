using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class LoginViewModel : VueModele, IPageViewModel
    {
        private ICommand _goToDraw;
        private ICommand _goToRegister;

        public ICommand GoToDraw
        {
            get
            {
                return _goToDraw ?? (_goToDraw = new RelayCommand(x =>
                {
                    Mediator.Notify("GoToDrawScreen", "");
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

    }
}
