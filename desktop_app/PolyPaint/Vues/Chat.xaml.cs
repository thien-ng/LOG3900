using System;
using System.Windows.Controls;
using System.Windows.Input;
using PolyPaint.VueModeles;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Chat.xaml
    /// </summary>
    public partial class Chat : UserControl
    {
        public Chat()
        {
            InitializeComponent();
            Keyboard.Focus(chatBox);
        }

        private void Button_Click(object sender, System.Windows.RoutedEventArgs e)
        {
            Keyboard.Focus(chatBox);
        }
    }
}
