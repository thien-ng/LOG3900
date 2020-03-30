using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.VueModeles
{
    class GameViewModel: BaseViewModel, IPageViewModel
    {
        public GameViewModel()
        {
            Console.WriteLine("GameViewModel");
            DrawViewModel = new DessinViewModel(800,800);
        }

        #region Public Attributes
        public DessinViewModel DrawViewModel { get; set; }
        #endregion

        #region Methods

        #endregion

        #region Commands

        #endregion
    }
}
