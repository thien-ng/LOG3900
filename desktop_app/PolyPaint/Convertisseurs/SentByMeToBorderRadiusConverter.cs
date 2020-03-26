using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class SentByMeToBorderRadiusConverter : BaseConverter<SentByMeToBorderRadiusConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? "10,0,10,10" : "0,10,10,10";
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => DependencyProperty.UnsetValue;
    }
}
