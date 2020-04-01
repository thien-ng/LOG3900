using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Convertisseurs
{
    class SentByServerToColorConverter : BaseConverter<SentByServerToColorConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            switch ((string)value)
            {
                case Constants.SENDER_SERVER:
                    return "White";

                case Constants.SENDER_ME:
                    return "LightGreen";

                default:
                    return "LightGray";
            }
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => System.Windows.DependencyProperty.UnsetValue;
    }
}
