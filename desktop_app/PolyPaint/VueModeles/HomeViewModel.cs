using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class HomeViewModel : BaseViewModel, IPageViewModel
    {
        private CancellationTokenSource _cts;

        public HomeViewModel()
        {
            Mediator.Subscribe("ChangeChannel", ChangeChannel);
            Mediator.Subscribe("SubToChannel", SubToChannel);
            Mediator.Subscribe("UnsubChannel", UnsubChannel);
            
            FetchChannels();
            
            _subChannels = new ObservableCollection<MessageChannel>();
            _notSubChannels = new ObservableCollection<MessageChannel>();
            _switchView = 0;
            _switchViewButton = "Profile";
            _switchViewButtonTooltip = "Access to profile";
            _frontEnabled = false;
            _backEnabled = false;
            _selectedChannel = new ChatRoom(Constants.DEFAULT_CHANNEL);
            _cts = new CancellationTokenSource();
            _searchString = "";
        }

        #region Public Attributes

        private ObservableCollection<MessageChannel> _subChannels;
        public ObservableCollection<MessageChannel> SubChannels
        {
            get { return _subChannels; }
            set { _subChannels = value; ProprieteModifiee(); }
        }

        private ObservableCollection<MessageChannel> _notSubChannels;
        public ObservableCollection<MessageChannel> NotSubChannels
        {
            get { return _notSubChannels; }
            set { _notSubChannels = value; ProprieteModifiee(); }
        }

        private ChatRoom _selectedChannel;
        public ObservableCollection<MessageChat> Messages
        {
            get { return _selectedChannel.Messages; }
        }

        private string _pendingMessage;
        public string PendingMessage
        {
            get { return _pendingMessage; }
            set { _pendingMessage = value; ProprieteModifiee(); }
        }

        private string _searchString;
        public string SearchString
        {
            get { return _searchString; }
            set
            {
                _searchString = value;
                FilterChannels();
                ProprieteModifiee();
            }
        }

        private int _switchView;
        public int SwitchView
        {
            get { return _switchView; }
            set { _switchView = value; ProprieteModifiee(); }
        }

        private string _switchViewButton;
        public string SwitchViewButton
        {
            get { return _switchViewButton; }
            set { _switchViewButton = value; ProprieteModifiee(); }
        }

        private string _switchViewButtonTooltip;
        public string SwitchViewButtonTooltip
        {
            get { return _switchViewButtonTooltip; }
            set { _switchViewButtonTooltip = value; ProprieteModifiee(); }
        }

        private bool _frontEnabled;
        public bool FrontEnabled
        {
            get { return _frontEnabled; }
            set { _frontEnabled = value; ProprieteModifiee(); }
        }

        private bool _backEnabled;
        public bool BackEnabled
        {
            get { return _backEnabled; }
            set { _backEnabled = value; ProprieteModifiee(); }
        }

        #endregion

        #region Methods

        private async void FetchChannels()
        {
            var subChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
            var notSubChannelReq = await ServerService.instance.client.GetAsync(Constants.SERVER_PATH + Constants.NOT_SUB_CHANNELS_PATH + "/" + ServerService.instance.username);
            
            ProcessChannelRequest(subChannelReq, _subChannels, true);

            _subChannels.SingleOrDefault(i => i.id == Constants.DEFAULT_CHANNEL).isSelected = true;

            ProcessChannelRequest(notSubChannelReq, _notSubChannels, false);
        }

        private async void ProcessChannelRequest(HttpResponseMessage response, ObservableCollection<MessageChannel> list, bool isSubbed)
        {
            if (response.IsSuccessStatusCode)
            {
                JArray responseJson = JArray.Parse(await response.Content.ReadAsStringAsync());
                foreach (JObject item in responseJson)
                {
                    if (item.ContainsKey("id"))
                    {
                        Application.Current.Dispatcher.Invoke(delegate
                        {
                            list.Add(new MessageChannel(item.GetValue("id").ToString(), isSubbed));
                        });
                    }
                }
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
            var response = await ServerService.instance.client.PutAsync(requestPath, new StringContent(""));

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
                _notSubChannels.Remove(_notSubChannels.SingleOrDefault(i => i.id == channelId));
                _subChannels.Add(joinedChannel);
            }
            else
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

        private void Disconnect()
        {
            ServerService.instance.socket.Emit("logout");
            ServerService.instance.username = "";
            Mediator.Notify("GoToLoginScreen", "");
        }

        private void FilterChannels()
        {
            _cts.Cancel();
            _cts = new CancellationTokenSource();

            string path = Constants.SERVER_PATH + Constants.SEARCH_CHANNEL_PATH + "/" + ServerService.instance.username + "/" + _searchString;
            ServerService.instance.client.GetAsync(path, _cts.Token).ContinueWith(responseTask => 
            {
                var response = responseTask.Result;
                response.Content.ReadAsStringAsync().ContinueWith(jsonTask =>
                {
                    var jArray = JArray.Parse(jsonTask.Result);

                    Application.Current.Dispatcher.Invoke(delegate
                    {
                        _subChannels.Clear();
                        _notSubChannels.Clear();

                        foreach (JObject item in jArray)
                        {
                            if (!(item.ContainsKey("id") && item.ContainsKey("sub")))
                                continue;

                            if (item.GetValue("sub").Value<bool>() == true)
                                _subChannels.Add(new MessageChannel(item.GetValue("id").ToString(), true));
                            else
                                _notSubChannels.Add(new MessageChannel(item.GetValue("id").ToString(), false));
                            ;
                        }
                    });
                });
            });
        }

        #endregion

        #region Commands

        private ICommand _disconnectCommand;
        public ICommand DisconnectCommand
        {
            get
            {
                return _disconnectCommand ?? (_disconnectCommand = new RelayCommand(x => Disconnect()));
            }
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
                    if (!FrontEnabled && !BackEnabled)
                    {
                        FrontEnabled = true;
                    }
                    FrontEnabled = BackEnabled;
                    BackEnabled = !BackEnabled;
                }));
            }
        }

        #endregion

    }
}
