using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Net.Http;
using System.Runtime.CompilerServices;

namespace PolyPaint.Modeles
{
    class MessageChannel: INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        public int ID { get; set; }
        private ObservableCollection<MessageChat> _items;

        public MessageChannel(int id)
        {
            ID = id;
            Setup();
        }

        private void Setup()
        {
            _items = new ObservableCollection<MessageChat>();
            ServerService.instance.socket.On("chat", data => ReceiveMessage((JObject)data));
        }

        public ObservableCollection<MessageChat> Items
        {
            get { return _items; }
            set { _items = value; ProprieteModifiee(); }
        }

        public void SendMessage(string message)
        {
            if (string.IsNullOrWhiteSpace(message))
                return;

            MessageSend messageToSend = new MessageSend(ServerService.instance.username, message, "general");

            var messageJson = JObject.FromObject(messageToSend);
            ServerService.instance.socket.Emit("chat", messageJson);
        }

        private void ReceiveMessage(JObject jsonMessage)
        {
            MessageReception message = jsonMessage.ToObject<MessageReception>();
            bool isSentByMe = message.username == ServerService.instance.username;
            App.Current.Dispatcher.Invoke(delegate
            {
                Items.Add(new MessageChat(message.username, message.content, isSentByMe, message.time));
            });
        }

        protected virtual void ProprieteModifiee([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
