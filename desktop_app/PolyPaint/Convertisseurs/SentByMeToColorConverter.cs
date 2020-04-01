using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class SentByMeToColorConverter : BaseConverter<SentByMeToColorConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? "LightGreen" : "LightGray";
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => System.Windows.DependencyProperty.UnsetValue;
    }
}
