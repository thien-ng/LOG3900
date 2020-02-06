using System;
using System.Windows.Controls;
using System.Windows.Input;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Chat.xaml
    /// </summary>
    public partial class Chat : Page
    {
        public Chat()
        {
            InitializeComponent();
            Keyboard.Focus(chatBox);
        }

        private void chatBox_LostKeyboardFocus(object sender, KeyboardFocusChangedEventArgs e)
        {
            Keyboard.Focus(chatBox);
        }
    }
}
