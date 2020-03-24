using System;
using System.Linq;
using System.Windows.Input;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace PolyPaint.VueModeles
{
    class CreateGameDrawViewModel: DessinViewModel
    {
        private double PerpendicularDistance(StylusPoint pt, StylusPoint lineStart, StylusPoint lineEnd)
        {
            double dx = lineEnd.X - lineStart.X;
            double dy = lineEnd.Y - lineStart.Y;

            // Normalize
            double mag = Math.Sqrt(dx * dx + dy * dy);
            if (mag > 0.0)
            {
                dx /= mag;
                dy /= mag;
            }
            double pvx = pt.X - lineStart.X;
            double pvy = pt.Y - lineStart.Y;

            // Get dot product (project pv onto normalized direction)
            double pvdot = dx * pvx + dy * pvy;

            // Scale line direction vector and subtract it from pv
            double ax = pvx - pvdot * dx;
            double ay = pvy - pvdot * dy;

            return Math.Sqrt(ax * ax + ay * ay);
        }

        private void RamerDouglasPeucker(List<StylusPoint> pointList, double epsilon, List<StylusPoint> output)
        {
            if (pointList.Count < 2) return;

            // Find the point with the maximum distance from line between the start and end
            double dmax = 0.0;
            int index = 0;
            int end = pointList.Count - 1;
            for (int i = 1; i < end; ++i)
            {
                double d = PerpendicularDistance(pointList[i], pointList[0], pointList[end]);
                if (d > dmax)
                {
                    index = i;
                    dmax = d;
                }
            }

            // If max distance is greater than epsilon, recursively simplify
            if (dmax > epsilon)
            {
                List<StylusPoint> recResults1 = new List<StylusPoint>();
                List<StylusPoint> recResults2 = new List<StylusPoint>();
                List<StylusPoint> firstLine = pointList.Take(index + 1).ToList();
                List<StylusPoint> lastLine = pointList.Skip(index).ToList();
                RamerDouglasPeucker(firstLine, epsilon, recResults1);
                RamerDouglasPeucker(lastLine, epsilon, recResults2);

                // build the result list
                output.AddRange(recResults1.Take(recResults1.Count - 1));
                output.AddRange(recResults2);
                if (output.Count < 2) throw new Exception("Problem assembling output");
            }
            else
            {
                // Just return start and end points
                output.Clear();
                output.Add(pointList[0]);
                output.Add(pointList[pointList.Count - 1]);
            }
        }

        public JArray GetDrawing()
        {

            JArray Strokes = new JArray();

            foreach (var trait in Traits)
            {
                JArray points = new JArray();

                foreach (var point in trait.StylusPoints)
                {
                    points.Add(new JObject(new JProperty("x", point.X),  
                                           new JProperty("y", point.Y)));
                }

                Strokes.Add(new JObject(new JProperty("color", trait.DrawingAttributes.Color.ToString()),
                                        new JProperty("width", trait.DrawingAttributes.Width),
                                        new JProperty("points", points.ToArray())));
            }

            return Strokes;
        }
    }
}
