using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Convertisseurs
{
    class SentByServerToColorConverter : BaseConverter<SentByMeToColorConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? "LightGray" : "LightGreen";
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => System.Windows.DependencyProperty.UnsetValue;
    }
}
