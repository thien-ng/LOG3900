using PolyPaint.Utilitaires;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;

namespace PolyPaint.Modeles
{
    public class GameCard: INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        public GameCard(string gameName,string mode)
        {
            _gameName = gameName;
            this.mode = mode;
        }

        public string mode { get; set; }
        private string _gameName;
        public string GameName
        {
            get { return _gameName; }
            set { _gameName = value; }
        }

        private ObservableCollection<Lobby> _gameLobbies;
        public ObservableCollection<Lobby> GameLobbies
        {
            get { return _gameLobbies; }
            set { _gameLobbies = value; ProprieteModifiee(nameof(GameLobbies)); }
        }
        protected virtual void ProprieteModifiee([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }

}
