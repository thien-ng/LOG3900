using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net.Http;
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class HomeViewModel : BaseViewModel, IPageViewModel
    {
        private string _pendingMessage;
        public string _switchViewButton;
        private int _switchView;
        private string _switchViewButtonTooltip;
        private bool _frontEnabled;
        private bool _backEnabled;
        private ObservableCollection<MessageChannel> _subChannels;
        private ObservableCollection<MessageChannel> _notSubChannels;
        private ChatRoom _selectedChannel;
        

        public HomeViewModel()
        {
            _subChannels = new ObservableCollection<MessageChannel>();
            _notSubChannels = new ObservableCollection<MessageChannel>();
            FetchChannels();
            Mediator.Subscribe("ChangeChannel", ChangeChannel);
            Mediator.Subscribe("SubToChannel", SubToChannel);
            Mediator.Subscribe("UnsubChannel", UnsubChannel);
            SwitchView = 0;
            SwitchViewButton = "Profile";
            SwitchViewButtonTooltip = "Access to profile";
            FrontEnabled = false;
            BackEnabled = false;
            _selectedChannel = new ChatRoom(Constants.DEFAULT_CHANNEL);
        }

        private async void FetchChannels()
        {
            var subChannelReq    = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
            var notSubChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.NOT_SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
            if (subChannelReq.IsSuccessStatusCode)
            {
                JArray responseJson = JArray.Parse(await subChannelReq.Content.ReadAsStringAsync());
                foreach (JObject item in responseJson)
                {
                    if (item.ContainsKey("id"))
                        _subChannels.Add(new MessageChannel(item.GetValue("id").ToString(), true));
                }
            }

            _subChannels.SingleOrDefault(i => i.id == Constants.DEFAULT_CHANNEL).isSelected = true;

            if (notSubChannelReq.IsSuccessStatusCode)
            {
                JArray responseJson = JArray.Parse(await notSubChannelReq.Content.ReadAsStringAsync());
                foreach (JObject item in responseJson)
                {
                    if (item.ContainsKey("id"))
                        _notSubChannels.Add(new MessageChannel(item.GetValue("id").ToString(), false));
                }
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

        public ObservableCollection<MessageChannel> SubChannels 
        {
            get { return _subChannels; }
            set { _subChannels = value; ProprieteModifiee(); }
        }

        public ObservableCollection<MessageChannel> NotSubChannels
        {
            get { return _notSubChannels; }
            set { _notSubChannels = value; ProprieteModifiee(); }
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
                _subChannels.SingleOrDefault(i => i.id == _selectedChannel.ID).isSelected = false;
                
                _selectedChannel = new ChatRoom((string)id);
                _subChannels.SingleOrDefault(i => i.id == _selectedChannel.ID).isSelected = true;
                
                ProprieteModifiee("Messages");
            }
        }

        private async void SubToChannel(object id)
        {
            string channelId = (string)id;
            string requestPath = Constants.SERVER_PATH + Constants.JOIN_CHANNEL_PATH + "/" + ServerService.instance.username + "/" + channelId;
            var response = await ServerService.instance.client.PutAsync(requestPath, new StringContent("")) ;

            if (!response.IsSuccessStatusCode)
            { 
                MessageBox.Show("Error while joining channel");
                return;
            }

            JObject responseJson = JObject.Parse(await response.Content.ReadAsStringAsync());

            if (!(responseJson.ContainsKey("status") && responseJson.ContainsKey("message")))
            {
                MessageBox.Show("Error parsing server response");
                return;
            }

            if (responseJson.GetValue("status").ToString() == "200")
            {
                MessageChannel joinedChannel = new MessageChannel(channelId, true);
                _notSubChannels.Remove(_notSubChannels.SingleOrDefault( i => i.id == channelId ));
                _subChannels.Add(joinedChannel);
            } else
                MessageBox.Show(responseJson.GetValue("message").ToString());



        }

        private async void UnsubChannel(object id)
        {
            string channelId = (string)id;
            string requestPath = Constants.SERVER_PATH + Constants.LEAVE_CHANNEL_PATH + "/" + ServerService.instance.username + "/" + channelId;
            var response = await ServerService.instance.client.DeleteAsync(requestPath);

            if (!response.IsSuccessStatusCode)
            {
                MessageBox.Show("Error while leaving channel");
                return;
            }

            JObject responseJson = JObject.Parse(await response.Content.ReadAsStringAsync());

            if (!(responseJson.ContainsKey("status") && responseJson.ContainsKey("message")))
            {
                MessageBox.Show("Error parsing server response");
                return;
            }

            if (responseJson.GetValue("status").ToString() == "200")
            {
                if (_selectedChannel.ID == channelId)
                    ChangeChannel(Constants.DEFAULT_CHANNEL);

                MessageChannel leftChannel = new MessageChannel(channelId, false);
                _subChannels.Remove(_subChannels.SingleOrDefault(i => i.id == channelId));
                _notSubChannels.Add(leftChannel);
            }
            else
                MessageBox.Show(responseJson.GetValue("message").ToString());
        }

        private ICommand _switchViewCommand;
        public ICommand SwitchViewCommand
        {
            get
            {
                return _switchViewCommand ?? (_switchViewCommand = new RelayCommand(x =>
                {
                    SwitchView = SwitchView == 0 ? 1 : 0;
                    SwitchViewButton = SwitchViewButton == "Profile" ? "GameList" : "Profile";
                    SwitchViewButtonTooltip = SwitchViewButtonTooltip == "Access to profile" ? "Access to gameList" : "Access to profile";
                    if(!FrontEnabled && !BackEnabled)
                    {
                        FrontEnabled = true;
                    }
                    FrontEnabled = BackEnabled;
                    BackEnabled = !BackEnabled;
                }));
            }
        }

        public int SwitchView
        {
            get { return _switchView; }
            set { _switchView = value; ProprieteModifiee(); }
        }

        public string SwitchViewButton
        {
            get { return _switchViewButton; }
            set { _switchViewButton = value; ProprieteModifiee(); }
        }

        public string SwitchViewButtonTooltip
        {
            get { return _switchViewButtonTooltip; }
            set { _switchViewButtonTooltip = value; ProprieteModifiee(); }
        }

        public bool FrontEnabled
        {
            get { return _frontEnabled; }
            set { _frontEnabled = value; ProprieteModifiee(); }
        }
        
        public bool BackEnabled
        {
            get { return _backEnabled; }
            set { _backEnabled = value; ProprieteModifiee(); }
        }

    }
}
