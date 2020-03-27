using System;
using System.Windows.Ink;
using System.Windows.Input;

namespace PolyPaint.Utilitaires
{
    class CustomStroke : Stroke
    {
        public CustomStroke(StylusPointCollection stylusPoints): base(stylusPoints) { }

        public CustomStroke(StylusPointCollection stylusPoints, DrawingAttributes attributes) : base(stylusPoints, attributes) { }

        public Guid uid { get; set; }
    }
}
