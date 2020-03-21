using PolyPaint.Utilitaires;
using System;
using System.Globalization;
using System.Windows;

namespace PolyPaint.Convertisseurs
{
    class CreationTypeToVisibilityConverter : BaseConverter<CreationTypeToVisibilityConverter>
    {
        public override object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            GameCreationObjects obj = (GameCreationObjects)Enum.Parse(typeof(GameCreationObjects), (string)parameter);
            CreationType type = (CreationType)value;
           
            switch (type)
            {
                case CreationType.Manual:
                    if (obj == GameCreationObjects.Canvas)
                        return Visibility.Visible;
                    else if (obj == GameCreationObjects.FileSelector)
                        return Visibility.Hidden;
                    else if (obj == GameCreationObjects.CanvasFileselector)
                        return Visibility.Visible;
                    else if (obj == GameCreationObjects.Generator)
                        return Visibility.Hidden;
                    break;
                
                case CreationType.Assisted1:
                    if (obj == GameCreationObjects.Canvas)
                        return Visibility.Hidden;
                    else if (obj == GameCreationObjects.FileSelector)
                        return Visibility.Visible;
                    else if (obj == GameCreationObjects.CanvasFileselector)
                        return Visibility.Visible;
                    else if (obj == GameCreationObjects.Generator)
                        return Visibility.Hidden;
                    break;
                
                case CreationType.Assisted2:
                    if (obj == GameCreationObjects.Canvas)
                        return Visibility.Hidden;
                    else if (obj == GameCreationObjects.FileSelector)
                        return Visibility.Hidden;
                    else if (obj == GameCreationObjects.CanvasFileselector)
                        return Visibility.Hidden;
                    else if (obj == GameCreationObjects.Generator)
                        return Visibility.Visible;
                    break;
                
                default:
                    throw new Exception("Type doesnt exist");
            }

            throw new Exception("Type or object doesnt exist");
        }

        public override object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
