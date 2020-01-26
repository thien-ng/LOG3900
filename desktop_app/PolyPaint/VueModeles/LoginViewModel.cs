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

    }
}
