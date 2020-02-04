using PolyPaint.Utilitaires;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Windows.Input;

namespace PolyPaint.VueModeles.Chat
{
    public class MessageListViewModel : BaseViewModel, IPageViewModel
    {
        private ICommand _sendCommand;
        private ObservableCollection<MessageItemViewModel> _list;

        public ICommand SendCommand
        {
            get
            {
                return _sendCommand ?? (_sendCommand = new RelayCommand(x => SendMessage()));
            }
        }

        public ObservableCollection<MessageItemViewModel> Items { 
            get { return _list; }
            set { _list = value; ProprieteModifiee(); }
        }

        public string PendingMessage { get; set; }

        private void SendMessage()
        {
            if (Items == null)
                Items = new ObservableCollection<MessageItemViewModel>();

            Items.Add(new MessageItemViewModel
            {
                Username = "Jeremy",
                Message = PendingMessage,
                SentByMe = true,
                TimeStamp = "10:55am"
            });

            ProprieteModifiee("Items");
        }
    }
}
