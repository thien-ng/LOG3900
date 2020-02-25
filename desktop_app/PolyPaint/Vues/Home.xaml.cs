using PolyPaint.VueModeles;
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
using System.ComponentModel;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Logique d'interaction pour Home.xaml
    /// </summary>
    public partial class Home : Page
    {
        bool _isOpen;
        Chat _chatView;
        Window _window;
        MessageListViewModel _chat;
        public Home()
        {
            InitializeComponent();
            if(App.Current.Windows.Count <= 2)
            {
                chatHome.Visibility = Visibility.Visible;
            }
            _isOpen = false;
            _chat = new MessageListViewModel();
            _chatView = new Chat();
            _window = new Window();
            _window.Content = _chatView;
            _window.Height = 450;
            _window.Width = 600;
            _window.DataContext = _chat;
            _window.Closing += new CancelEventHandler(this.onWindowClosing);
        }
        private void onWindowClosing(Object sender, CancelEventArgs e)
        {
            e.Cancel =true;
            _window.Hide();
            chatHome.Visibility = Visibility.Visible;
            _isOpen = false;
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            if(!_isOpen) { 
                _window.Show();
                chatHome.Visibility = Visibility.Collapsed;
                _isOpen = true;
            }
            else
            {
                _window.Hide();
                chatHome.Visibility = Visibility.Visible;
                _isOpen = false;
            }
        }
    }
}
