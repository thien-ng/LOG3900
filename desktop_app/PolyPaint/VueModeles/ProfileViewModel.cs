
namespace PolyPaint.VueModeles
{
    class ProfileViewModel
    {
        string _username;
        public ProfileViewModel()
        {
            _username = Services.ServerService.instance.username;
        }

        public string Username
        {
            get { return _username; }
        }
    }
}
