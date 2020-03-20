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
        public bool IsLobbyChat { get; set; }

        public ChatRoom(string id, bool isLobbyChat)
        {
            _messages = new ObservableCollection<MessageChat>();
            ID = id;
            IsLobbyChat = isLobbyChat;
            if (isLobbyChat) { SetupLobbyChat(); }
            else { Setup(); }
            
        }

        private void SetupLobbyChat()
        {
            ServerService.instance.socket.On("lobby-chat", data => ReceiveMessage((JObject)data));
        }

        private void Setup()
        {
            LoadMessages();
            ServerService.instance.socket.On("chat", data => ReceiveMessage((JObject)data));
        }

        public ObservableCollection<MessageChat> Messages
        {
            get { return _messages; }
            set { _messages = value; ProprieteModifiee(nameof(Messages)); }
        }

        public void SendMessage(string message)
        {
            if (string.IsNullOrWhiteSpace(message))
                return;


            if (IsLobbyChat)
            {
                var newMessage = new JObject(new JProperty("lobbyName", ID),
                                             new JProperty("username", ServerService.instance.username),
                                             new JProperty("message", message));
                ServerService.instance.socket.Emit("lobby-chat", newMessage);
            }
            else
            {
                MessageSend messageToSend = new MessageSend(ServerService.instance.username, message, ID);

                var messageJson = JObject.FromObject(messageToSend);
                ServerService.instance.socket.Emit("chat", messageJson);
            }
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
