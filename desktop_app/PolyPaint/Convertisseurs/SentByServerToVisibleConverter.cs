using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Convertisseurs
{
    class SentByServerToVisibleConverter : BaseConverter<SentByServerToVisibleConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            switch ((string)value)
            {
                case Constants.SENDER_SERVER:
                    return "Hidden";

                case Constants.SENDER_ME:
                    return "Hidden";

                default:
                    return "Visible";
            }
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => System.Windows.DependencyProperty.UnsetValue;
    }
}
