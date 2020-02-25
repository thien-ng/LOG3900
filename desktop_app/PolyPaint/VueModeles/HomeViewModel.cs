using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System.Collections.ObjectModel;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class HomeViewModel : BaseViewModel, IPageViewModel
    {
        private string _pendingMessage;
        private ChatRoom _selectedChannel;

        public HomeViewModel()
        {
            _selectedChannel = new ChatRoom("general");
            Mediator.Subscribe("ChangeChannel", ChangeChannel);
        }

        private ICommand _goToLogin;
        public ICommand GoToLogin
        {
            get
            {
                return _goToLogin ?? (_goToLogin = new RelayCommand(x =>
                {
                    Mediator.Notify("GoToLoginScreen", "");
                }));
            }
        }

        private ICommand _disconnectCommand;
        public ICommand DisconnectCommand
        {
            get
            {
                return _disconnectCommand ?? (_disconnectCommand = new RelayCommand(x => Disconnect()));
            }
        }

        private void Disconnect()
        {
            ServerService.instance.socket.Emit("logout");
            ServerService.instance.username = "";
            Mediator.Notify("GoToLoginScreen", "");
            //TODO Channel ID 1 temp
        }

        public ObservableCollection<MessageChat> Messages
        {
            get { return _selectedChannel.Messages; }
        }

        public string PendingMessage
        {
            get { return _pendingMessage; }
            set { _pendingMessage = value; ProprieteModifiee(); }
        }

        private ICommand _sendCommand;
        public ICommand SendCommand
        {
            get
            {
                return _sendCommand ?? (_sendCommand = new RelayCommand<string>(x =>
                {
                    _selectedChannel.SendMessage(PendingMessage);
                    PendingMessage = "";
                }));
            }
        }

        private void ChangeChannel(object id)
        {
            _selectedChannel = new ChatRoom((string)id);
            ProprieteModifiee("Messages");
        }
        public ObservableCollection<MessageChannel> Test
        {
            get { return new ObservableCollection<MessageChannel> { new MessageChannel("general"), new MessageChannel("pute")}; }
        }
    }
}
