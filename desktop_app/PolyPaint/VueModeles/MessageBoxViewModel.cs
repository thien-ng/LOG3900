using MaterialDesignThemes.Wpf;
using PolyPaint.Utilitaires;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class MessageBoxViewModel: BaseViewModel
    {

        private string _message;
        public string Message 
        { 
            get { return _message; } 
            set { _message = value; ProprieteModifiee(); } 
        }

        private ICommand _closeCommand;
        public ICommand CloseCommand
        {
            get
            {
                return _closeCommand ?? (_closeCommand = new RelayCommand(x =>
                {
                    DialogHost.CloseDialogCommand.Execute(null, null);
                }));
            }
        }
    }

}
