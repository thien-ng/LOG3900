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
    class SentByServerAlignementConverter : BaseConverter<SentByServerAlignementConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            switch ((string)value)
            {
                case Constants.SENDER_SERVER: 
                    return HorizontalAlignment.Center;

                case Constants.SENDER_ME: 
                    return HorizontalAlignment.Right;

                default:  
                    return HorizontalAlignment.Left;
            }
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => DependencyProperty.UnsetValue;
    }
}
