using PolyPaint.VueModeles;
using System.Windows;
using System.Windows.Controls;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Login.xaml
    /// </summary>
    public partial class Login : Page
    {
        public Login()
        {
            InitializeComponent();
        }

        private void PasswordChanged(object sender, RoutedEventArgs e)
        {
            if (DataContext != null)
            { 
                ((LoginViewModel)DataContext).Password = ((PasswordBox)sender);
                ((LoginViewModel)DataContext).OnPasswordPropertyChanged();
            }
        }
    }
}
