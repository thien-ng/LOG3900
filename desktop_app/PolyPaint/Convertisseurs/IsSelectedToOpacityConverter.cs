using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class IsSelectedToOpacityConverter : BaseConverter<IsSelectedToOpacityConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? "0.3": "0";
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => DependencyProperty.UnsetValue;
    }
}
