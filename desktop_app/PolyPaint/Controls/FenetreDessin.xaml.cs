using System;
using System.Windows;
using System.Windows.Input;
using System.Windows.Controls.Primitives;
using System.Windows.Controls;
using PolyPaint.VueModeles;
using System.Collections.Generic;

namespace PolyPaint.Controls
{
    /// <summary>
    /// Logique d'interaction pour FenetreDessin.xaml
    /// </summary>
    public partial class FenetreDessin : Grid
    {
        public FenetreDessin()
        {
            InitializeComponent();
        }

        // Pour gérer les points de contrôles.
        //private void GlisserCommence(object sender, DragStartedEventArgs e) => (sender as Thumb).Background = Brushes.Black;
        //private void GlisserTermine(object sender, DragCompletedEventArgs e) => (sender as Thumb).Background = Brushes.White;
        private void GlisserMouvementRecu(object sender, DragDeltaEventArgs e)
        {
            string nom = (sender as Thumb).Name;
            if (nom == "horizontal" || nom == "diagonal") colonne.Width = new GridLength(Math.Max(32, colonne.Width.Value + e.HorizontalChange));
            if (nom == "vertical" || nom == "diagonal") ligne.Height = new GridLength(Math.Max(32, ligne.Height.Value + e.VerticalChange));
        }

        // Pour la gestion de l'affichage de position du pointeur.
        private void surfaceDessin_MouseLeave(object sender, MouseEventArgs e) => textBlockPosition.Text = "";

        private void surfaceDessin_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(surfaceDessin);
            textBlockPosition.Text = Math.Round(p.X) + ", " + Math.Round(p.Y) + "px";

            bool isLBPressedInsideCanvas = e.LeftButton == MouseButtonState.Pressed &&
                                           p.X >= 0 && p.X <= surfaceDessin.ActualWidth &&
                                           p.Y >= 0 && p.Y <= surfaceDessin.ActualHeight;

            if (isLBPressedInsideCanvas)
                ((DessinViewModel)DataContext).MouseMove(surfaceDessin, e);
        }

        private void OnMouseDown(object sender, MouseButtonEventArgs e)
        {
            ((DessinViewModel)DataContext).IsDrawing = true;
            ((DessinViewModel)DataContext).IsEndOfStroke = false;
        }

        private void OnMouseUp(object sender, MouseButtonEventArgs e)
        {
            ((DessinViewModel)DataContext).OnEndOfStroke(surfaceDessin, e);
        }

    }
}
