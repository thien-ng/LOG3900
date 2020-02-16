using PolyPaint.VueModeles.Chat;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using PolyPaint;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Logique d'interaction pour Home.xaml
    /// </summary>
    public partial class Home : Page
    {
        private int _instanceCounter;
        public Home()
        {
            InitializeComponent();
            _instanceCounter = 0;
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            if(App.Current.Windows.Count <= 2) { 
            MessageListViewModel chat = new MessageListViewModel();
            Chat chatView = new Chat();
            Window window = new Window();
            window.Content = chatView;
            window.DataContext = chat;
            window.Show();
            chatHome.Visibility = Visibility.Collapsed;

            }
        }
    }
}
