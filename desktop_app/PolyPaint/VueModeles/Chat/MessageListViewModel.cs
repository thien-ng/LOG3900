using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.ObjectModel;
using System.Windows.Input;

namespace PolyPaint.VueModeles.Chat
{
    public class MessageListViewModel : BaseViewModel, IPageViewModel
    {
        private int counter = 0;
        private ICommand _sendCommand;
        private string _pendingMessage;
        private ObservableCollection<MessageItemViewModel> _list;

        public MessageListViewModel()
        {
            Setup();
        }

        ////// vvvvTemporaire pour le prototype vvvv//////

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
            ServerService.instance.socket.Emit("disconnect");
            ServerService.instance.username = "";
            Mediator.Notify("GoToLoginScreen", "");
        }

        //////^^^^ Temporaire pour le prototype ^^^^//////

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

        public string PendingMessage
        {
            get { return _pendingMessage; }
            set { _pendingMessage = value; ProprieteModifiee(); }
        }

        private void Setup()
        {
            Items = new ObservableCollection<MessageItemViewModel>();
            ServerService.instance.socket.On("chat", data => ReceiveMessage((JObject)data));
        }

        private void ReceiveMessage(JObject response)
        {
            Message test = response.ToObject<Message>();

            bool con = test.username == ServerService.instance.username;

            App.Current.Dispatcher.Invoke(delegate
            {
                Items.Add(new MessageItemViewModel
                {
                    Message = test.content,
                    SentByMe =con,
                    Username = test.username,
                    TimeStamp = "10:55am"
                });
            });
        }

        private void SendMessage()
        {
            if (string.IsNullOrWhiteSpace(PendingMessage))
                return;

            Message message = new Message(ServerService.instance.username, PendingMessage, 1);

            var messageJson = JObject.FromObject(message);
            ServerService.instance.socket.Emit("chat", messageJson);

            PendingMessage = "";
        }
    }
}
