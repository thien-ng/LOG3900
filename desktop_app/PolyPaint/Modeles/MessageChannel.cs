
using PolyPaint.Utilitaires;
using System;
using System.Windows.Input;

namespace PolyPaint.Modeles
{
    public class MessageChannel
    {
        public string id { get; set; }

        public MessageChannel(string id)
        {
            this.id = id;
        }

        private ICommand _selectChannelCommand;
        public ICommand SelectChannelCommand
        {
            get
            {
                return _selectChannelCommand ?? (_selectChannelCommand = new RelayCommand(x =>
                {
                    Mediator.Notify("ChangeChannel", id);
                }));
            }
        }
    }
}
