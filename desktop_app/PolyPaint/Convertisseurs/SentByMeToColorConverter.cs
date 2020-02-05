using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class SentByMeToColorConverter : BaseConverter<SentByMeToColorConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? "LightGray" : "LightGreen";
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
