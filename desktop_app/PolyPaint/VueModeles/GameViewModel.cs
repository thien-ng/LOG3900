using Newtonsoft.Json.Linq;
using PolyPaint.Controls;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class GameViewModel: BaseViewModel, IPageViewModel
    {
        public GameViewModel()
        {
            DrawViewModel = new DessinViewModel();
            _points = new ObservableCollection<PointsDisplay>();
            ServerService.instance.socket.On("game-drawer", data => processRole((JObject)data));
            ServerService.instance.socket.On("game-timer", data => processTime((JObject)data));
            ServerService.instance.socket.On("game-over", data => processEndGame((JObject)data));
        }

        #region Public Attributes
        public DessinViewModel DrawViewModel { get; set; }

        private string _role;
        public string Role
        {
            get { return _role; }
            set { _role = value; ProprieteModifiee(); }
        }

        private string _timer;
        public string Timer
        {
            get { return _timer; }
            set { _timer = value; ProprieteModifiee(); }
        }

        private string _objectToDraw;
        public string ObjectToDraw
        {
            get { return _objectToDraw; }
            set { _objectToDraw = value; ProprieteModifiee(); }
        }

        private ObservableCollection<PointsDisplay> _points;
        public ObservableCollection<PointsDisplay> Points
        {
            get { return _points; }
            set { _points = value; ProprieteModifiee(); }
        }

        private object _dialogContent;
        public object DialogContent
        {
            get { return _dialogContent; }
            set
            {
                if (_dialogContent == value) return;
                _dialogContent = value;
                ProprieteModifiee();
            }
        }

        private bool _isEndGameDialogOpen;
        public bool IsEndGameDialogOpen
        {
            get { return _isEndGameDialogOpen; }
            set
            {
                if (_isEndGameDialogOpen == value) return;
                _isEndGameDialogOpen = value;
                ProprieteModifiee();
            }
        }
        #endregion

        #region Methods
        private void setupGame()
        {
            var gameReady = new JObject(
                new JProperty("event", "ready"),
                new JProperty("username", ServerService.instance.username));
            ServerService.instance.socket.Emit("gameplay", gameReady);
        }

        private void processRole(JObject role) 
        {
            if (role.GetValue("username").ToString() == ServerService.instance.username)
            {
                Role = Constants.ROLE_DRAWER;
                ObjectToDraw = role.GetValue("object").ToString();
                Mediator.Notify("updateRole", true);
            }
            else
            {
                Role = Constants.ROLE_GUESSER;
                ObjectToDraw = "";
                Mediator.Notify("updateRole", false);
            }
            Mediator.Notify("clearDraw");
        }

        private void processTime(JObject time)
        {
            Timer = time.GetValue("time").ToString();
        }

        private void processEndGame(JObject pointsReceived)
        {

            JArray a = (JArray)pointsReceived["points"];

            IList<Points> points = a.ToObject<IList<Points>>();
            int i = 1;
            foreach (var item in points)
            {
                PointsDisplay temp = new PointsDisplay(item.username, item.points, i++); 
                Points.Add(temp);
            }
            App.Current.Dispatcher.Invoke(delegate
            {
                DialogContent = new EndGameControl();
                IsEndGameDialogOpen = true;
            });

        }
        #endregion

        #region Commands
        private ICommand _loadedCommand;
        public ICommand LoadedCommand
        {
            get
            {
                return _loadedCommand ?? (_loadedCommand = new RelayCommand(x =>
                {
                    setupGame();
                }));
            }
        }

        private ICommand _okCommand;
        public ICommand OkCommand
        {
            get
            {
                return _okCommand ?? (_okCommand = new RelayCommand(x =>
                {
                    IsEndGameDialogOpen = false;
                    Mediator.Notify("goToGameListView");
                }));
            }
        }
        #endregion
    }
}
