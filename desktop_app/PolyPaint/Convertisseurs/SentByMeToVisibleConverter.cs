using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class SentByMeToVisibleConverter : BaseConverter<SentByMeToVisibleConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? "Hidden" : "Visible";
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => System.Windows.DependencyProperty.UnsetValue;
    }
}
