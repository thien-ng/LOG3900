using System;
using System.Windows.Controls;
using System.Windows.Input;
using PolyPaint.VueModeles.Chat;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Chat.xaml
    /// </summary>
    public partial class Chat : UserControl
    {
        public Chat()
        {
            this.DataContext = new MessageListViewModel();
            InitializeComponent();
            Keyboard.Focus(chatBox);
        }

        private void Button_Click(object sender, System.Windows.RoutedEventArgs e)
        {
            Keyboard.Focus(chatBox);
        }
    }
}
