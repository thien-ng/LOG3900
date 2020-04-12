
using MaterialDesignThemes.Wpf;
using PolyPaint.Controls;
using PolyPaint.VueModeles;

namespace PolyPaint.Utilitaires
{
    class MessageBoxDisplayer
    {
        private static CustomMessageBoxControl view = new CustomMessageBoxControl();
        private static MessageBoxViewModel model = new MessageBoxViewModel();

        public static void ShowMessageBox(string message)
        {
            model.Message = message;
            view.DataContext = model;
            DialogHost.Show(view, "RootDialog");
        }
    }
}
