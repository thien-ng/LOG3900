
using System;

namespace PolyPaint.Modeles
{
    public class HintModel
    {
        public string Hint { get; set; }

        public Guid Uid { get; set; }

        public bool IsFirst { get; }

        public HintModel(bool isFirst)
        {
            Hint = "";
            Uid = Guid.NewGuid();
            IsFirst = isFirst;
        }
    }
}
