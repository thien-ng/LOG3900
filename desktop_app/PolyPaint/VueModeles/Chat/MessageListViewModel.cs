﻿using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.ObjectModel;
using System.Windows.Input;

namespace PolyPaint.VueModeles.Chat
{
    public class MessageListViewModel : BaseViewModel, IPageViewModel
    {
        private ICommand _sendCommand;
        private string _pendingMessage;
        private MessageChannel _channel;

        public MessageListViewModel()
        {
            //TODO Channel ID 1 temp
            _channel = new MessageChannel(1);
        }

        ////// vvvvTemporaire pour le prototype vvvv//////

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
            _channel = new MessageChannel(1);
        }

        //////^^^^ Temporaire pour le prototype ^^^^//////

        public ICommand SendCommand
        {
            get
            {
                return _sendCommand ?? (_sendCommand = new RelayCommand<string>(x => 
                {
                    _channel.SendMessage(PendingMessage);
                    PendingMessage = "";
                }));
            }
        }

        public ObservableCollection<MessageChat> Items 
        { 
            get { return _channel.Items; }
        }

        public string PendingMessage
        {
            get { return _pendingMessage; }
            set { _pendingMessage = value; ProprieteModifiee(); }
        }
    }
}