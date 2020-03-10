﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Controls;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;

namespace PolyPaint.VueModeles
{
    class DessinViewModel : BaseViewModel, IPageViewModel
    {

        private Editeur editeur = new Editeur();

        // Ensemble d'attributs qui définissent l'apparence d'un trait.
        public DrawingAttributes AttributsDessin { get; set; } = new DrawingAttributes();

        public string OutilSelectionne
        {
            get { return editeur.OutilSelectionne; }
            set { ProprieteModifiee(); }
        }

        public string CouleurSelectionnee
        {
            get { return editeur.CouleurSelectionnee; }
            set { editeur.CouleurSelectionnee = value; }
        }

        public string PointeSelectionnee
        {
            get { return editeur.PointeSelectionnee; }
            set { ProprieteModifiee(); }
        }

        public int TailleTrait
        {
            get { return editeur.TailleTrait; }
            set { editeur.TailleTrait = value; }
        }

        private StrokeCollection _traits;
        public StrokeCollection Traits 
        {
            get { return _traits; }
            set { _traits = value; ProprieteModifiee(); }
        }

        // Commandes sur lesquels la vue pourra se connecter.
        public RelayCommand<string> ChoisirPointe { get; set; }
        public RelayCommand<string> ChoisirOutil { get; set; }

        /// <summary>
        /// Constructeur de VueModele
        /// On récupère certaines données initiales du modèle et on construit les commandes
        /// sur lesquelles la vue se connectera.
        /// </summary>
        public DessinViewModel()
        {
            ServerService.instance.socket.On("draw", data => OnDraw((JObject)data));
            // On écoute pour des changements sur le modèle. Lorsqu'il y en a, EditeurProprieteModifiee est appelée.
            editeur.PropertyChanged += new PropertyChangedEventHandler(EditeurProprieteModifiee);

            // On initialise les attributs de dessin avec les valeurs de départ du modèle.
            AttributsDessin = new DrawingAttributes();
            AttributsDessin.Color = (Color)ColorConverter.ConvertFromString(editeur.CouleurSelectionnee);
            AjusterPointe();

            Traits = editeur.traits;

            // Pour chaque commande, on effectue la liaison avec des méthodes du modèle.
            // Pour les commandes suivantes, il est toujours possible des les activer.
            // Donc, aucune vérification de type Peut"Action" à faire.
            ChoisirPointe = new RelayCommand<string>(editeur.ChoisirPointe);
            ChoisirOutil = new RelayCommand<string>(editeur.ChoisirOutil);

            previousPos = new Dictionary<string, double?> { { "X", null }, { "Y", null } };
        }

        /// <summary>
        /// Traite les évènements de modifications de propriétés qui ont été lancés à partir
        /// du modèle.
        /// </summary>
        /// <param name="sender">L'émetteur de l'évènement (le modèle)</param>
        /// <param name="e">Les paramètres de l'évènement. PropertyName est celui qui nous intéresse. 
        /// Il indique quelle propriété a été modifiée dans le modèle.</param>
        private void EditeurProprieteModifiee(object sender, PropertyChangedEventArgs e)
        {
            switch (e.PropertyName)
            {
                case "CouleurSelectionnee":
                    AttributsDessin.Color = (Color)ColorConverter.ConvertFromString(editeur.CouleurSelectionnee);
                    break;
                case "OutilSelectionne":
                    OutilSelectionne = editeur.OutilSelectionne;
                    break;
                case "PointeSelectionnee":
                    PointeSelectionnee = editeur.PointeSelectionnee;
                    AjusterPointe();
                    break;
                default:
                    AjusterPointe();
                    break;
            }
        }

        /// <summary>
        /// C'est ici qu'est défini la forme de la pointe, mais aussi sa taille (TailleTrait).
        /// Pourquoi deux caractéristiques se retrouvent définies dans une même méthode? Parce que pour créer une pointe 
        /// horizontale ou verticale, on utilise une pointe carrée et on joue avec les tailles pour avoir l'effet désiré.
        /// </summary>
        private void AjusterPointe()
        {
            AttributsDessin.StylusTip = (editeur.PointeSelectionnee == "ronde") ? StylusTip.Ellipse : StylusTip.Rectangle;
            AttributsDessin.Width = editeur.TailleTrait;
            AttributsDessin.Height = editeur.TailleTrait;
        }

        private bool _isDrawing;
        public bool IsDrawing
        {
            get { return _isDrawing; }
            set { _isDrawing = value; ProprieteModifiee(); }
        }

        public Dictionary<string, double?> previousPos { get; set; }

        private ICommand _goToLogin;
        public ICommand GoToLogin
        {
            get
            {
                return _goToLogin ?? (_goToLogin = new RelayCommand(x =>
                {
                    Mediator.Notify("GoToLoginScreen", "");
                }));
            }
        }

        public void MouseMove(InkCanvas sender, MouseEventArgs e)
        {
            if (IsDrawing)
            {
                if (previousPos["X"] == null || previousPos["Y"] == null)
                {
                    previousPos["X"] = e.GetPosition(sender).X;
                    previousPos["Y"] = e.GetPosition(sender).Y;

                    return;
                }

                dynamic values = new JObject();
                
                values.username  = ServerService.instance.username;
                values.startPosX = previousPos["X"];
                values.startPosY = previousPos["Y"];
                values.endPosX   = e.GetPosition(sender).X;
                values.endPosY   = e.GetPosition(sender).Y;
                values.color     = editeur.CouleurSelectionnee;
                values.width     = editeur.TailleTrait;

                previousPos["X"] = e.GetPosition(sender).X;
                previousPos["Y"] = e.GetPosition(sender).Y;

                ServerService.instance.socket.Emit("gameplay", values);
            }
        }

        public void OnDraw(JObject data) 
        {
            double X1 = (double)data.GetValue("startPosX");
            double X2 = (double)data.GetValue("endPosX");
            double Y1 = (double)data.GetValue("startPosY");
            double Y2 = (double)data.GetValue("endPosY");

            StylusPointCollection coll = new StylusPointCollection();
            coll.Add(new StylusPoint(X1, Y1));
            coll.Add(new StylusPoint(X2, Y2));

            Stroke str = new Stroke(coll);

            App.Current.Dispatcher.Invoke(delegate
            {
                Traits.Add(str);
            });
        }
    }
}
