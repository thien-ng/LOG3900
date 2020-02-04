namespace PolyPaint.VueModeles.Chat
{
    public class MessageItemViewModel : BaseViewModel
    {
        public string Username  { get; set; }

        public string Message   {  get; set; }

        public bool   SentByMe  { get; set; }

        public string TimeStamp { get; set; }
    }
}
