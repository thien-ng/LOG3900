using System.Windows;
using System.Windows.Controls;
using System.ComponentModel;
using PolyPaint.VueModeles;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Logique d'interaction pour Home.xaml
    /// </summary>
    public partial class Home : UserControl
    {
        private bool _isOpen;
        private Chat _chatView;
        private Window _window;

        public Home()
        {
            InitializeComponent();
            if(App.Current.Windows.Count <= 2)
            {
                chatHome.Visibility = Visibility.Visible;
            }
            _isOpen = false;
            _chatView = new Chat();
            _window = new Window();
            _window.Content = _chatView;
            _window.Height = 450;
            _window.Width = 600;
            _window.Closing += new CancelEventHandler(this.onWindowClosing);
        }

        public void PageLoaded(object sender, RoutedEventArgs e) 
        { 
            _window.DataContext = DataContext;
        }

        private void onWindowClosing(object sender, CancelEventArgs e)
        {
            e.Cancel = true;
            _window.Hide();
            chatColumn.Width = new GridLength(2, GridUnitType.Star);
            chatHome.Visibility = Visibility.Visible;

            _isOpen = false;
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            if(!_isOpen) { 
                _window.Show();
                chatColumn.Width = GridLength.Auto;
                chatHome.Visibility = Visibility.Collapsed;
                _isOpen = true;
            }
            else
            {
                _window.Hide();
                chatColumn.Width = new GridLength(2, GridUnitType.Star);
                chatHome.Visibility = Visibility.Visible;
                _isOpen = false;
            }
        }
    }
}
