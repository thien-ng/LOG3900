using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class IsSubbedToVisibilityConverter : BaseConverter<IsSubbedToVisibilityConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? Visibility.Visible: Visibility.Hidden;
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => DependencyProperty.UnsetValue;
    }
}
