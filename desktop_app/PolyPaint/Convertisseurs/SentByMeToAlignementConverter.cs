using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class SentByMeToAlignementConverter : BaseConverter<SentByMeToAlignementConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (bool)value ? HorizontalAlignment.Right : HorizontalAlignment.Left;
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
