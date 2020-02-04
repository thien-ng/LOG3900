using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace PolyPaint.VueModeles.Chat.Design
{
    class MessageListDesignModel : MessageListViewModel
    {
        public static MessageListDesignModel Instance => new MessageListDesignModel();

        public MessageListDesignModel()
        {
            Items = new ObservableCollection<MessageItemViewModel>
            {
                new MessageItemViewModel
                {
                    Username = "Jeremy",
                    Message = "Sed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis unde ",
                    SentByMe = true,
                    TimeStamp = "10:54am",
                },

                new MessageItemViewModel
                {
                    Username = "Arthur",
                    Message = "Sed ut perspiciatis undeSed ut perspiciatis undeSed ut",
                    SentByMe = false,
                    TimeStamp = "10:55am",
                },

                new MessageItemViewModel
                {
                    Username = "Thien",
                    Message = "Sed ut perspiciatis undeSed ut peiatis undeSed ut peiatis undeSed ut perspiciatis undeSed ut",
                    SentByMe = false,
                    TimeStamp = "10:56am",
                },

                new MessageItemViewModel
                {
                    Username = "Jacob",
                    Message = "Sed ut perspiciatisiatis undeSed ut peiatis undeSed ut pe ut perspiciatis undeSed ut",
                    SentByMe = false,
                    TimeStamp = "10:57am",
                },

                new MessageItemViewModel
                {
                    Username = "FX",
                    Message = "Sed ut piatis undeSed ut peiatis undeSed ut peiatis undeSed ut peerspiciatis undeSed ut perspiciatis undeSed ut",
                    SentByMe = false,
                    TimeStamp = "10:58am",
                },

                new MessageItemViewModel
                {
                    Username = "Pablo",
                    Message = "Sed ut perspiciatis eSed ut",
                    SentByMe = false,
                    TimeStamp = "10:59am",
                },

            };
        }

    }
}
