using Newtonsoft.Json.Linq;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class GameViewModel: BaseViewModel, IPageViewModel
    {
        public GameViewModel()
        {
            DrawViewModel = new DessinViewModel();
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
            } else
            {
                Role = Constants.ROLE_GUESSER;
                ObjectToDraw = "";
            }
        }

        private void processTime(JObject time)
        {
            Timer = time.GetValue("time").ToString();
        }

        private void processEndGame(JObject points)
        {
            Console.WriteLine("ProcessEndGame");
            try
            {
                if (points.ContainsKey("username"))
                    Console.WriteLine(points.GetValue("points").ToString());
            }
            catch (Exception)
            {
                Mediator.Notify("goToGameListView");
            }
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
        #endregion
    }
}
