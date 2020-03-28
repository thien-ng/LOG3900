using PolyPaint.Utilitaires;
using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class PanoramicModeToVisibilityConverter : BaseConverter<PanoramicModeToVisibilityConverter>
    {

        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            DisplayMode obj = (DisplayMode)Enum.Parse(typeof(DisplayMode), (string)value);
            switch (obj)
            {
                case DisplayMode.Centered:
                    return Visibility.Hidden;
                case DisplayMode.Classic:
                    return Visibility.Hidden;
                case DisplayMode.Random:
                    return Visibility.Hidden;
                case DisplayMode.Panoramic:
                    return Visibility.Visible;
                default:
                    throw new Exception("Mode doesnt exist");
            }
            throw new Exception("Mode or object doesnt exist");
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }


}
