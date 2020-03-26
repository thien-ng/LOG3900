using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class IsFirstToVisibilityConverter : BaseConverter<IsFirstToVisibilityConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? Visibility.Hidden : Visibility.Visible;
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => DependencyProperty.UnsetValue;
    }
}
