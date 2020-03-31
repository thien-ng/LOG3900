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

        public ChatRoom(string id, bool isLobbyChat)
        {
            _messages = new ObservableCollection<MessageChat>();
            _messagesGame = new ObservableCollection<MessageGame>();
            ID = id;
            IsLobbyChat = isLobbyChat;
            _isInGameChat = false;
            if(id == Constants.GAME_CHANNEL) { SetupGameChat(); return; }
            if (isLobbyChat) { SetupLobbyChat(); }
            else { Setup(); }
        }

        #region Public Attributes
        public event PropertyChangedEventHandler PropertyChanged;

        public string ID { get; set; }

        public bool IsLobbyChat { get; set; }

        private bool _isInGameChat;

        public bool IsInGameChat {
            get { return _isInGameChat; } 
            set { _isInGameChat = value; ProprieteModifiee(); Console.WriteLine(_isInGameChat); }
        }

        private ObservableCollection<MessageChat> _messages;
        public ObservableCollection<MessageChat> Messages
        {
            get { return _messages; }
            set { _messages = value; ProprieteModifiee(nameof(Messages)); }
        }

        private ObservableCollection<MessageGame> _messagesGame;
        public ObservableCollection<MessageGame> MessagesGame
        {
            get { return _messagesGame; }
            set { _messagesGame = value; ProprieteModifiee(nameof(MessagesGame)); }
        }

        #endregion

        #region Methods

        private void SetupGameChat()
        {
            IsInGameChat = true;
            ServerService.instance.socket.On("game-chat", data => ReceiveGameMessage((JObject)data));
        }

        private void SetupLobbyChat()
        {
            IsInGameChat = false;
            ServerService.instance.socket.On("lobby-chat", data => ReceiveMessage((JObject)data));
        }

        private void Setup()
        {
            IsInGameChat = false;
            LoadMessages();
            ServerService.instance.socket.On("chat", data => ReceiveMessage((JObject)data));
        }
        public void SendMessage(string message)
        {
            if (string.IsNullOrWhiteSpace(message))
                return;

            if (IsInGameChat)
            {
                var newMessage = new JObject(new JProperty("event", "chat"),
                                             new JProperty("username", ServerService.instance.username),
                                             new JProperty("content", message));
                ServerService.instance.socket.Emit("gameplay", newMessage);
            }
            if (IsLobbyChat)
            {
                var newMessage = new JObject(new JProperty("lobbyName", ID),
                                             new JProperty("username", ServerService.instance.username),
                                             new JProperty("content", message));
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

        private void ReceiveGameMessage(JToken jsonMessage)
        {
            MessageGame message = jsonMessage.ToObject<MessageGame>();
            Console.WriteLine(message.content);

            App.Current.Dispatcher.Invoke(delegate
            {
                MessagesGame.Add(new MessageGame(message.username, message.content, message.isServer));
            });
            Console.WriteLine(MessagesGame.Count);
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
        #endregion
    }
}
