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
    class ModeToGuessleftVisibilityConverter: BaseConverter<ModeToGuessleftVisibilityConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            switch ((string)value)
            {
                case Constants.MODE_COOP:
                    return "Visible";

                case Constants.MODE_SOLO:
                    return "Visible";

                default:
                    return "Hidden";
            }
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => DependencyProperty.UnsetValue;
    }
}
