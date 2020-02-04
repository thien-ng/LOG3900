namespace PolyPaint.VueModeles.Chat.Design
{
    class MessageItemDesignModel : MessageItemViewModel
    {
        public static MessageItemDesignModel Instance => new MessageItemDesignModel();

        public MessageItemDesignModel()
        {
            Username = "Jeremy";
            Message = "Sed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis undeSed ut perspiciatis unde ";
            SentByMe = true;
            TimeStamp = "10:54am";
        }

    }
}
