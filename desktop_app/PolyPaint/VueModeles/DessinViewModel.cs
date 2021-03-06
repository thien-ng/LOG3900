﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows;
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
    class DessinViewModel : BaseViewModel, IPageViewModel, IDisposable
    {



        /// <summary>
        /// Constructeur de VueModele
        /// On récupère certaines données initiales du modèle et on construit les commandes
        /// sur lesquelles la vue se connectera.
        /// </summary>
        /// 
        public DessinViewModel()
        {
            Setup();
        }

        #region Public Attributes
        // Ensemble d'attributs qui définissent l'apparence d'un trait.
        private Editeur editeur = new Editeur();
        private Guid currentStrokeId = Guid.NewGuid();
        private int eraserDiameter;

        private bool _isDrawer;
        public bool IsDrawer
        {
            get { return _isDrawer; }
            set { _isDrawer = value; ProprieteModifiee(); } 
        }

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

        private bool _isDrawing;
        public bool IsDrawing
        {
            get { return _isDrawing; }
            set { _isDrawing = value; ProprieteModifiee(); }
        }

        public Dictionary<string, double?> previousPos { get; set; }

        public bool IsEndOfStroke { get; set; }

        #endregion

        #region Methods

        private void Setup()
        {

            ServerService.instance.socket.On("draw", data => ReceiveDrawing((JObject)data));

            editeur.PropertyChanged += new PropertyChangedEventHandler(EditeurProprieteModifiee);

            // On initialise les attributs de dessin avec les valeurs de départ du modèle.
            AttributsDessin = new DrawingAttributes();
            AttributsDessin.Color = (Color)ColorConverter.ConvertFromString(editeur.CouleurSelectionnee);
            AjusterPointe();

            Traits = editeur.traits;
            IsDrawer = true;

            // Pour chaque commande, on effectue la liaison avec des méthodes du modèle.
            // Pour les commandes suivantes, il est toujours possible des les activer.
            // Donc, aucune vérification de type Peut"Action" à faire.
            ChoisirPointe = new RelayCommand<string>(editeur.ChoisirPointe);
            ChoisirOutil = new RelayCommand<string>(editeur.ChoisirOutil);

            previousPos = new Dictionary<string, double?> { { "X", null }, { "Y", null } };
            eraserDiameter = 8;
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

        public void MouseOutOfBounds(InkCanvas sender, MouseEventArgs e)
        {
            if (editeur.OutilSelectionne == "crayon")
            {
                if (previousPos["X"] == null || previousPos["Y"] == null)
                    return;

                previousPos["X"] = e.GetPosition(sender).X;
                previousPos["Y"] = e.GetPosition(sender).Y;

                IsEndOfStroke = true;

                DrawingInk(sender, e);

                previousPos["X"] = null;
                previousPos["Y"] = null;
                IsEndOfStroke = false;
            }
        }

        public void MouseMove(InkCanvas sender, MouseEventArgs e)
        {
            if (!IsDrawing) return;

            if (previousPos["X"] == null || previousPos["Y"] == null)
            {
                previousPos["X"] = e.GetPosition(sender).X;
                previousPos["Y"] = e.GetPosition(sender).Y;
            }

            switch (editeur.OutilSelectionne)
            {
                case "crayon":
                    DrawingInk(sender, e);
                    break;
                case "efface_segment":
                case "efface_trait":
                    DrawingEraser(sender, e);
                    break;
                default:
                    break;
            }

            previousPos["X"] = e.GetPosition(sender).X;
            previousPos["Y"] = e.GetPosition(sender).Y;
        }

        public void ReceiveDrawing(JObject data) 
        {
            if (!data.ContainsKey("type")) return;

            switch ((string)data.GetValue("type"))
            {
                case "ink":
                    ReceiveDrawingInk(data);
                    break;
                case "eraser":
                    if ((string)data.GetValue("eraser") == "stroke")
                        ReceiveEraseStroke(data);
                    else
                        ReceiveErasePoint(data);
                    break;
            }
        }

        private void DrawingInk(InkCanvas sender, MouseEventArgs e)
        {
            string format = editeur.PointeSelectionnee == "ronde" ? "circle" : "square";

            JObject drawing = new JObject(new JProperty("event", "draw"),
                                          new JProperty("username", ServerService.instance.username),
                                          new JProperty("startPosX", previousPos["X"]),
                                          new JProperty("startPosY", previousPos["Y"]),
                                          new JProperty("endPosX", e.GetPosition(sender).X),
                                          new JProperty("endPosY", e.GetPosition(sender).Y),
                                          new JProperty("color", editeur.CouleurSelectionnee),
                                          new JProperty("width", editeur.TailleTrait),
                                          new JProperty("isEnd", IsEndOfStroke),
                                          new JProperty("format", format),
                                          new JProperty("type", "ink"));

            ServerService.instance.socket.Emit("gameplay", drawing);

            ReceiveDrawing(drawing);
        }

        private void DrawingEraser(InkCanvas sender, MouseEventArgs e)
        {
            try
            {
                string eraserType;
                int? eraserWidth = null;

                if (editeur.OutilSelectionne == "efface_segment")
                {
                    eraserType = "point";
                    eraserWidth = editeur.TailleTrait;
                }
                else
                    eraserType = "stroke";

                JObject eraser = new JObject(new JProperty("event", "draw"),
                                             new JProperty("username", ServerService.instance.username),
                                             new JProperty("type", "eraser"),
                                             new JProperty("x", e.GetPosition(sender).X),
                                             new JProperty("y", e.GetPosition(sender).Y),
                                             new JProperty("eraser", eraserType));

                if (eraserWidth != null)
                    eraser.Add("width", eraserWidth);

                ServerService.instance.socket.Emit("gameplay", eraser);
            }
            catch (Exception)
            {
                // fail silently
            }
        } 

        private void ReceiveDrawingInk(JObject data)
        {
            try
            {

                double? X1 = (double?)data.GetValue("startPosX");
                double? X2 = (double?)data.GetValue("endPosX");
                double? Y1 = (double?)data.GetValue("startPosY");
                double? Y2 = (double?)data.GetValue("endPosY");

                if (!(X1.HasValue && X2.HasValue && Y1.HasValue && Y2.HasValue))
                    return;

                StylusPointCollection coll = new StylusPointCollection();
                coll.Add(new StylusPoint(X1.Value, Y1.Value));
                coll.Add(new StylusPoint(X2.Value, Y2.Value));

                DrawingAttributes attr = new DrawingAttributes();
                attr.Color = (Color)ColorConverter.ConvertFromString((string)data.GetValue("color"));
                attr.Height = (double)data.GetValue("width");
                attr.Width = attr.Height;
                attr.StylusTip = (string)data.GetValue("format") == "circle" ? StylusTip.Ellipse : StylusTip.Rectangle;

                CustomStroke str = new CustomStroke(coll, attr);
                str.uid = currentStrokeId;

                App.Current.Dispatcher.Invoke(delegate
                {
                    Traits.Add(str);
                });

                if ((bool)data.GetValue("isEnd"))
                    MergeStrokes(attr);
            }
            catch (Exception)
            {
                // fail silently
            }
            
        }

        private void ReceiveEraseStroke(JObject data)
        {
            try
            {
                double x = (double)data.GetValue("x");
                double y = (double)data.GetValue("y");

                StrokeCollection coll = Traits.HitTest(new Point(x, y), eraserDiameter);

                App.Current.Dispatcher.Invoke(delegate
                {
                    Traits.Remove(coll);
                });
            }
            catch (Exception)
            {
                // fail silently
            }
        }

        private void ReceiveErasePoint(JObject data)
        {
            try
            {
                double x = (double)data.GetValue("x");
                double y = (double)data.GetValue("y");


                StrokeCollection strokes = Traits.HitTest(new Point(x, y), eraserDiameter);

                if (strokes.Count == 0) 
                    return;

                Rect rect = new Rect(x - (eraserDiameter / 2),
                                     y - (eraserDiameter / 2),
                                     eraserDiameter,
                                     eraserDiameter);

                foreach (var stroke in strokes)
                {
                    int strokeIndex = Traits.IndexOf(stroke);
                    
                    StrokeCollection splitStroke = stroke.GetEraseResult(rect);
                    
                    App.Current.Dispatcher.Invoke(delegate
                    {
                        foreach (var item in splitStroke)
                        {
                            ((CustomStroke)item).uid = Guid.NewGuid();

                            Traits.Insert(strokeIndex, item);
                        }

                        Traits.Remove(stroke);
                    });
                }
            }
            catch { /*fail silently*/ }
        }

        private void MergeStrokes(DrawingAttributes attr)
        {
            try
            {
                StylusPointCollection points = new StylusPointCollection();
                StrokeCollection strokesToRemove = new StrokeCollection();

                foreach (CustomStroke trait in Traits)
                {
                    if (trait.uid == currentStrokeId)
                    {
                        points.Add(trait.StylusPoints);
                        strokesToRemove.Add(trait);
                    }
                }

                CustomStroke fullStroke = new CustomStroke(points, attr);
                fullStroke.uid = currentStrokeId;

                App.Current.Dispatcher.Invoke(delegate
                {
                    Traits.Add(fullStroke);
                    Traits.Remove(strokesToRemove);
                });

                currentStrokeId = Guid.NewGuid();

            }
            catch (Exception)
            {
                App.Current.Dispatcher.Invoke(delegate
                {
                    Traits.Clear();
                });
            }
        }

        public void OnEndOfStroke(InkCanvas sender, MouseEventArgs e)
        {
            try
            {
                IsEndOfStroke = true;

                if (editeur.OutilSelectionne == "crayon")
                    DrawingInk(sender, e);

                IsDrawing = false;
                previousPos = new Dictionary<string, double?> { { "X", null }, { "Y", null } };
            }
            catch (Exception)
            {
                // fail silently
            }
        }

        public override void Dispose()
        {
            ServerService.instance.socket.Off("draw");
        }

        #endregion

        #region Commands

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
        #endregion
    }
}
