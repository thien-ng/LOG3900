using System;
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
    class DessinViewModel : BaseViewModel, IPageViewModel
    {

        

        /// <summary>
        /// Constructeur de VueModele
        /// On récupère certaines données initiales du modèle et on construit les commandes
        /// sur lesquelles la vue se connectera.
        /// </summary>
        /// 
        public DessinViewModel()
        {
            Width = 1000;
            Height = 800;

            Setup();
        }

        public DessinViewModel(int width, int height)
        {
            Width = width;
            Height = height;

            Setup();
        }



        #region Public Attributes
        // Ensemble d'attributs qui définissent l'apparence d'un trait.
        private Editeur editeur = new Editeur();
        private Guid currentStrokeId = Guid.NewGuid();
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

        public int Width { get; set; }
        
        public int Height { get; set; }

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

            Console.WriteLine(data);

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

            int color = int.Parse(editeur.CouleurSelectionnee.Remove(0,1), System.Globalization.NumberStyles.HexNumber);

            JObject drawing = new JObject(new JProperty("event", "draw"),
                                          new JProperty("username", ServerService.instance.username),
                                          new JProperty("startPosX", previousPos["X"]),
                                          new JProperty("startPosY", previousPos["Y"]),
                                          new JProperty("endPosX", e.GetPosition(sender).X),
                                          new JProperty("endPosY", e.GetPosition(sender).Y),
                                          new JProperty("color", color),
                                          new JProperty("width", editeur.TailleTrait),
                                          new JProperty("isEnd", IsEndOfStroke),
                                          new JProperty("format", format),
                                          new JProperty("type", "ink"));

            ServerService.instance.socket.Emit("gameplay", drawing);
        }

        private void DrawingEraser(InkCanvas sender, MouseEventArgs e)
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

        private void ReceiveDrawingInk(JObject data)
        {
            double X1 = (double)data.GetValue("startPosX");
            double X2 = (double)data.GetValue("endPosX");
            double Y1 = (double)data.GetValue("startPosY");
            double Y2 = (double)data.GetValue("endPosY");

            StylusPointCollection coll = new StylusPointCollection();
            coll.Add(new StylusPoint(X1, Y1));
            coll.Add(new StylusPoint(X2, Y2));

            byte[] colorBytes = BitConverter.GetBytes((int)data.GetValue("color"));
            if (!BitConverter.IsLittleEndian) Array.Reverse(colorBytes);
            Color color = colorBytes.Length == 4 ? Color.FromArgb(colorBytes[0], colorBytes[1], colorBytes[2], colorBytes[3]) : (Color)ColorConverter.ConvertFromString("#FF000000");

            DrawingAttributes attr = new DrawingAttributes();
            attr.Color = color;
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

        private void ReceiveEraseStroke(JObject data)
        {
            double x = (double)data.GetValue("x");
            double y = (double)data.GetValue("y");

            StrokeCollection coll = Traits.HitTest(new Point(x, y));

            App.Current.Dispatcher.Invoke(delegate
            {
                Traits.Remove(coll);
            });
        }

        private void ReceiveErasePoint(JObject data)
        {
            double x = (double)data.GetValue("x");
            double y = (double)data.GetValue("y");

            StrokeCollection strokes = Traits.HitTest(new Point(x, y));

            if (strokes.Count == 0) return;

            foreach (var stroke in strokes)
            {
                Rect rect = new Rect(x - 5, y - 5, 10, 10);
                StrokeCollection splitStroke = stroke.GetEraseResult(rect);

                App.Current.Dispatcher.Invoke(delegate
                {
                    foreach (var item in splitStroke)
                    {
                        ((CustomStroke)item).uid = Guid.NewGuid();
                        Traits.Add(item);
                    }

                    Traits.Remove(stroke);
                });
            }
        }

        private void MergeStrokes(DrawingAttributes attr)
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

            App.Current.Dispatcher.Invoke(delegate
            {
                Traits.Remove(strokesToRemove);
            });

            CustomStroke fullStroke = new CustomStroke(points, attr);
            fullStroke.uid = currentStrokeId;

            App.Current.Dispatcher.Invoke(delegate
            {
                Traits.Add(fullStroke);
            });

            currentStrokeId = Guid.NewGuid();
        }

        public void OnEndOfStroke(InkCanvas sender, MouseEventArgs e)
        {
            IsEndOfStroke = true;

            if (editeur.OutilSelectionne == "crayon")
                DrawingInk(sender, e);

            IsDrawing = false;
            previousPos = new Dictionary<string, double?> { { "X", null }, { "Y", null } };
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

        private ICommand _loadedCommand;
        public ICommand LoadedCommand
        {
            get
            {
                return _loadedCommand ?? (_loadedCommand = new RelayCommand(x =>
                {
                    JObject clientReady = new JObject(new JProperty("username", ServerService.instance.username),
                                                      new JProperty("event", "ready"));

                    ServerService.instance.socket.Emit("gameplay", clientReady);
                }));
            }
        }
        #endregion
    }
}
