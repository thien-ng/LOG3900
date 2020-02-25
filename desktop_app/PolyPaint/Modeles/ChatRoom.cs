using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace PolyPaint.Modeles
{
    class ChatRoom: INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        public string ID { get; set; }
        private ObservableCollection<MessageChat> _messages;

        public ChatRoom(string id)
        {
            ID = id;
            Setup();
        }

        private void Setup()
        {
            _messages = new ObservableCollection<MessageChat>();
            LoadMessages();
            ServerService.instance.socket.On("chat", data => ReceiveMessage((JObject)data));
        }

        public ObservableCollection<MessageChat> Messages
        {
            get { return _messages; }
            set { _messages = value; ProprieteModifiee(); }
        }

        public void SendMessage(string message)
        {
            if (string.IsNullOrWhiteSpace(message))
                return;

            MessageSend messageToSend = new MessageSend(ServerService.instance.username, message, ID);

            var messageJson = JObject.FromObject(messageToSend);
            ServerService.instance.socket.Emit("chat", messageJson);
        }

        private void ReceiveMessage(JToken jsonMessage)
        {
            MessageReception message = jsonMessage.ToObject<MessageReception>();
            bool isSentByMe = message.username == ServerService.instance.username;
            App.Current.Dispatcher.Invoke(delegate
            {
                Messages.Add(new MessageChat(message.username, message.content, isSentByMe, message.time));
            });
        }

        private async void LoadMessages()
        {
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.CHAT_MESSAGES_PATH + "/" + ID);
            JArray responseJson = JArray.Parse(await response.Content.ReadAsStringAsync());

            foreach (var item in responseJson)
                ReceiveMessage(item);
        }

        protected virtual void ProprieteModifiee([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
