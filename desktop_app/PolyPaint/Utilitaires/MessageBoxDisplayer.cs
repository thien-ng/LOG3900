
using MaterialDesignThemes.Wpf;
using PolyPaint.Controls;
using PolyPaint.VueModeles;
using System.Threading.Tasks;

namespace PolyPaint.Utilitaires
{
    class MessageBoxDisplayer
    {
        private static CustomMessageBoxControl view = new CustomMessageBoxControl();
        private static MessageBoxViewModel model = new MessageBoxViewModel();

        public static async Task<object> ShowMessageBox(string message)
        {
            model.Message = message;
            view.DataContext = model;
            return await DialogHost.Show(view, "RootDialog");
        }
    }
}
