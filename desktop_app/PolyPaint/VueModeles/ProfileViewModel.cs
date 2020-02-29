using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PolyPaint.Services;

namespace PolyPaint.VueModeles
{
    class ProfileViewModel
    {
        String _username;
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
