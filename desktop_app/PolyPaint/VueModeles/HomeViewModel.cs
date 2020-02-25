using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System.Collections.ObjectModel;
using System.Windows.Controls;
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
        private ChatRoom _selectedChannel;

        public HomeViewModel()
        {
            _selectedChannel = new ChatRoom("general");
            Mediator.Subscribe("ChangeChannel", ChangeChannel);
            SwitchView = 0;
            SwitchViewButton = "Profile";
            SwitchViewButtonTooltip = "Access to profile";
            FrontEnabled = false;
            BackEnabled = false;
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
            get { return new ObservableCollection<MessageChannel> { new MessageChannel("general"), new MessageChannel("pute") }; }
        }
        //private object _selectedView;
        //public object SelectedView {
        //    get { return _selectedView; }
        //    set { _selectedView = value; ProprieteModifiee("SelectedView"); }
        //}
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
