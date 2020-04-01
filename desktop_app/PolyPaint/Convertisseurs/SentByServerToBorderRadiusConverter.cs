using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class SentByServerToBorderRadiusConverter : BaseConverter<SentByServerToBorderRadiusConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            switch ((string)value)
            {
                case Constants.SENDER_SERVER:
                    return "0,0,0,0";

                case Constants.SENDER_ME:
                    return "10,0,10,10";

                default:
                    return "0,10,10,10";
            }
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => DependencyProperty.UnsetValue;
    }
}
