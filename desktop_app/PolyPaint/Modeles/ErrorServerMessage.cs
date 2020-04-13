using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class ErrorServerMessage
    {
        public string message { get; set; }
        public ErrorServerMessage(string message)
        {
            this.message = message;
        }
    }
}
