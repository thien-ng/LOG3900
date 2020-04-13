using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using System.Windows.Controls;
using System.Globalization;

namespace PolyPaint.Utilitaires
{
    class UsernameRule : ValidationRule
    {
        private Regex userNameFormat = new Regex("^[a-zA-Z][a-zA-Z0-9]*$");
        public UsernameRule() 
        {          }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string username = "";
            try
            {
                if (((string)value).Length > 0)
                    username = value.ToString();
            }
            catch (Exception e)
            {
                return new ValidationResult(false, $"Illegal characters or {e.Message}");
            }

            if (!userNameFormat.IsMatch(username))
            {
                return new ValidationResult(false,
                  $"Username must be alphanumeric");
            }
            if ((username.Length < Constants.USR_MIN_LENGTH) || (username.Length >= 20))
            { 
                return new ValidationResult(false,
                  $"Username must have between 1 and 20 characters");
            }
                return ValidationResult.ValidResult;
        }
    }
    class ChannelNameRule : ValidationRule
    {
        private Regex userNameFormat = new Regex("^[a-zA-Z][a-zA-Z0-9]*$");
        public ChannelNameRule()
        { }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string name = "";
            try
            {
                if (((string)value).Length > 0)
                    name = value.ToString();
            }
            catch (Exception e)
            {
                return new ValidationResult(false, $"Illegal characters or {e.Message}");
            }

            if (!userNameFormat.IsMatch(name))
            {
                return new ValidationResult(false,
                  $"Channel name must be alphanumeric");
            }
            if ((name.Length < Constants.USR_MIN_LENGTH) || (name.Length >= 20))
            {
                return new ValidationResult(false,
                  $"Channel name must have between 1 and 20 characters");
            }
            return ValidationResult.ValidResult;
        }
    }
}
