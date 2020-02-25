using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System.Collections.ObjectModel;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class HomeViewModel : BaseViewModel, IPageViewModel
    {
        private ObservableCollection<MessageChannel> _channels;
        private ChatRoom _selectedChannel;
        private string _pendingMessage;
        

        public HomeViewModel()
        {
            _channels = new ObservableCollection<MessageChannel>();
            FetchChannels();
            Mediator.Subscribe("ChangeChannel", ChangeChannel);
            _selectedChannel = new ChatRoom("pute");
        }

        private async void FetchChannels()
        {
            var response = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.USER_CHANNELS_PATH + "/" + ServerService.instance.username);
            JArray responseJson = JArray.Parse(await response.Content.ReadAsStringAsync());

            foreach (var item in responseJson)
            {
                if (((JObject)item).ContainsKey("id"))
                    _channels.Add(new MessageChannel(((JObject)item).GetValue("id").ToString()));
            }
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

        public ObservableCollection<MessageChannel> Channels 
        {
            get { return _channels; }
            set { _channels = value; ProprieteModifiee(); }
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
            string channelId = (string)id;
            
            if (channelId != _selectedChannel.ID)
            {
                _selectedChannel = new ChatRoom((string)id);
                ProprieteModifiee("Messages");
            }
        }
    }
}
