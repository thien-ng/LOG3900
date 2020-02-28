using PolyPaint.Utilitaires;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;

namespace PolyPaint.Modeles
{
    public class MessageChannel: INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        public string id { get; set; }

        private bool _isSelected;
        public bool isSelected 
        { 
            get { return _isSelected; } 
            set { _isSelected = value; ProprieteModifiee(); } 
        }


        private bool _isSubbed;
        public bool isSubbed { 
            get { return _isSubbed && id != Constants.DEFAULT_CHANNEL; }
            set { _isSubbed = value; }
        }

        public MessageChannel(string id, bool isSubbed)
        {
            this.id = id;
            this.isSubbed = isSubbed;
            isSelected = false;
        }

        private ICommand _selectChannelCommand;
        public ICommand SelectChannelCommand
        {
            get
            {
                return _selectChannelCommand ?? (_selectChannelCommand = new RelayCommand(x =>
                {
                    if (isSubbed || id == Constants.DEFAULT_CHANNEL)
                        Mediator.Notify("ChangeChannel", id);
                    else
                        Mediator.Notify("SubToChannel", id);
                }));
            }
        }

        private ICommand _unsubCommand;
        public ICommand UnsubCommand
        {
            get
            {
                return _unsubCommand ?? (_unsubCommand = new RelayCommand(x =>
                {
                    _isSubbed = false;
                    Mediator.Notify("UnsubChannel", id);
                }));
            }
        }

        protected virtual void ProprieteModifiee([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
