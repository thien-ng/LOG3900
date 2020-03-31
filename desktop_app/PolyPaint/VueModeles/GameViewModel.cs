using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class GameViewModel: BaseViewModel, IPageViewModel
    {
        public GameViewModel()
        {
            DrawViewModel = new DessinViewModel(800,800);
        }

        #region Public Attributes
        public DessinViewModel DrawViewModel { get; set; }
        #endregion

        #region Methods

        #endregion

        #region Commands
        private ICommand _loadedCommand;
        public ICommand LoadedCommand
        {
            get
            {
                return _loadedCommand ?? (_loadedCommand = new RelayCommand(x =>
                {
                    ServerService.instance.socket.Emit("game-start");
                }));
            }
        }
        #endregion
    }
}
