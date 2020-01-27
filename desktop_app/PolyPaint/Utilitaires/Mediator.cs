using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Utilitaires
{
    public static class Mediator
    {
        private static IDictionary<string, List<Action<object>>> place_dictionary =
           new Dictionary<string, List<Action<object>>>();

        public static void Subscribe(string token, Action<object> callback)
        {
            if (!place_dictionary.ContainsKey(token))
            {
                var list = new List<Action<object>>();
                list.Add(callback);
                place_dictionary.Add(token, list);
            }
            else
            {
                bool found = false;
                foreach (var item in place_dictionary[token])
                    if (item.Method.ToString() == callback.Method.ToString())
                        found = true;
                if (!found)
                    place_dictionary[token].Add(callback);
            }
        }

        public static void Unsubscribe(string token, Action<object> callback)
        {
            if (place_dictionary.ContainsKey(token))
                place_dictionary[token].Remove(callback);
        }

        public static void Notify(string token, object args = null)
        {
            if (place_dictionary.ContainsKey(token))
                foreach (var callback in place_dictionary[token])
                    callback(args);
        }
    }
}
