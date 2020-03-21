using System;
using System.Globalization;
using System.IO;
using System.Windows.Media.Imaging;

namespace PolyPaint.Convertisseurs
{
    class Base64ImageConverter: BaseConverter<Base64ImageConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string img = value as string;

            if (img == null)
                return null;

            BitmapImage bi = new BitmapImage();

            bi.BeginInit();
            bi.StreamSource = new MemoryStream(System.Convert.FromBase64String(img));
            bi.EndInit();

            return bi;
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }

    }
}
