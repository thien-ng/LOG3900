
using MaterialDesignThemes.Wpf;
using Microsoft.Win32;
using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;

namespace PolyPaint.VueModeles
{
    class CreateGameViewModel: BaseViewModel
    {

        private JArray GeneratedImageStrokes;

        public CreateGameViewModel()
        {
            Difficulty = new ObservableCollection<string> { "Easy", "Medium", "Hard" };
            DisplayMode = new ObservableCollection<string> { "Classic", "Random", "Panoramic", "Centered" };
            PanoramicMode = new ObservableCollection<string> { "Up", "Down", "Right", "Left" };
            Hints = new ObservableCollection<HintModel> { new HintModel(true) };
            DrawViewModel = new CreateGameDrawViewModel();
            SelectedCreationType = CreationType.Manual;
            SelectedDisplayMode = DisplayMode[0];
            SelectedDifficulty = Difficulty[1];
            SelectedPanoramicMode = PanoramicMode[0];
            IsReqActive = true;
        }


        #region Public Attributes

        public ObservableCollection<string> Difficulty { get; set; }
        public ObservableCollection<string> DisplayMode { get; set; }
        public ObservableCollection<string> PanoramicMode { get; set; }
        public CreateGameDrawViewModel DrawViewModel { get; set; }

        private ObservableCollection<HintModel> _hints;
        public ObservableCollection<HintModel> Hints
        {
            get { return _hints; }
            set { _hints = value; ProprieteModifiee(); }
        }

        private string _solution;
        public string Solution
        {
            get { return _solution; }
            set
            {
                if (_solution == value) return;
                _solution = value;
                ProprieteModifiee();
            }
        }

        private string _base64ImageData;
        public string Base64ImageData
        { 
            get { return _base64ImageData; }
            set 
            {
                if (_base64ImageData == value) return;
                _base64ImageData = value;
                ProprieteModifiee();
            } 
        }

        private string _selectedDifficulty;
        public string SelectedDifficulty
        {
            get { return _selectedDifficulty; }
            set
            {
                if (_selectedDifficulty == value) return;
                _selectedDifficulty = value;
                ProprieteModifiee();
            }
        }

        // TODO add drawpxl
        private string _objectName;
        public string ObjectName
        {
            get { return _objectName; }
            set
            {
                if (_objectName == value) return;
                _objectName = value;
                ProprieteModifiee();
            }
        }

        private bool _isReqActive;
        public bool IsReqActive
        {
            get { return _isReqActive; }
            set
            {
                if (_isReqActive == value) return;
                _isReqActive = value;
                ProprieteModifiee();
            }
        }

        private CreationType _selectedCreationType;
        public CreationType SelectedCreationType
        {
            get { return _selectedCreationType; }
            set
            {
                if (_selectedCreationType == value) return;
                _selectedCreationType = value;
                ProprieteModifiee();
            }
        }

        private string _selectedDisplayMode;
        public string SelectedDisplayMode
        {
            get { return _selectedDisplayMode; }
            set
            {
                if (_selectedDisplayMode == value) return;
                _selectedDisplayMode = value;
                ProprieteModifiee();
            }
        }

        private string _selectedPanoramicMode;
        public string SelectedPanoramicMode
        {
            get { return _selectedPanoramicMode; }
            set
            {
                if (_selectedPanoramicMode == value) return;
                _selectedPanoramicMode = value;
                ProprieteModifiee();
            }
        }

        private BitmapImage _selectedImage;
        public BitmapImage SelectedImage
        {
            get { return _selectedImage; }
            set
            {
                if (_selectedImage == value) return;
                _selectedImage = value;
                ProprieteModifiee();
            }
        }

        #endregion

        #region Methods

        private async Task<HttpResponseMessage> CreateManual()
        {
            JArray drawing = DrawViewModel.GetDrawing();
            List<string> clues = new List<string>();

            foreach (var hint in Hints)
                clues.Add(hint.Hint);

            var newGame = new JObject( new JProperty("solution", Solution),
                                       new JProperty("clues", clues.ToArray()),
                                       new JProperty("difficulty", SelectedDifficulty.ToLower()),
                                       new JProperty("drawing", drawing),
                                       new JProperty("displayMode", SelectedDisplayMode.ToLower()),
                                       new JProperty("side", SelectedPanoramicMode.ToLower()));

            var content = new StringContent(newGame.ToString(), Encoding.UTF8, "application/json");

            return await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.GAMECREATOR_PATH, content);
        }

        // TODO 
        private void CreateAssisted1()
        {

        }

        private async Task<HttpResponseMessage> CreateAssisted2()
        {
            List<string> clues = new List<string>();

            foreach (var hint in Hints)
            {
                if (hint.Hint.Length > 0)
                clues.Add(hint.Hint);
            }

            var newGame = new JObject(new JProperty("solution", ObjectName),
                                       new JProperty("clues", clues.ToArray()),
                                       new JProperty("difficulty", SelectedDifficulty.ToLower()),
                                       new JProperty("drawing", GeneratedImageStrokes),
                                       new JProperty("displayMode", SelectedDisplayMode));

            var content = new StringContent(newGame.ToString(), Encoding.UTF8, "application/json");

            return await ServerService.instance.client.PostAsync(Constants.SERVER_PATH + Constants.GAMECREATOR_PATH, content);
        }

        private async Task Accept()
        {
            HttpResponseMessage result;
            switch (SelectedCreationType)
            {
                case CreationType.Manual:
                    result = await CreateManual();

                    if (!result.IsSuccessStatusCode)
                    {
                        MessageBox.Show("Invalid request");
                        return;
                    }
                    break;
                case CreationType.Assisted1:
                    CreateAssisted1();
                    break;
                case CreationType.Assisted2:
                    result = await CreateAssisted2();

                    if (!result.IsSuccessStatusCode)
                    {
                        MessageBox.Show("Invalid request");
                        return;
                    }
                    break;
                default:
                    break;
            }

            JObject res = new JObject(new JProperty("IsAccept", true));
            DialogHost.CloseDialogCommand.Execute(res, null);
        }

        private void RemoveHint(object x)
        {
            Guid Uid = (Guid)x;
            int indexToRemove = -1;

            foreach (var hint in Hints)
            {
                if (hint.Uid == Uid)
                {
                    indexToRemove = Hints.IndexOf(hint);
                    break;
                }
            }

            if (indexToRemove != -1)
                Hints.RemoveAt(indexToRemove);
        }

        private void SelectCreationType(object x)
        {
            string creationType = (string)x;

            switch (creationType)
            {
                case "man":
                    SelectedCreationType = CreationType.Manual;
                    break;
                case "ass1":
                    SelectedCreationType = CreationType.Assisted1;
                    break;
                case "ass2":
                    SelectedCreationType = CreationType.Assisted2;
                    break;
                default:
                    SelectedCreationType = CreationType.Manual;
                    break;
            }
        }

        private async void GenerateQuickDraw(object x)
        {
            IsReqActive = false;
            var requestPath = Constants.SERVER_PATH + Constants.SUGGESTION_PATH;
            var response = await ServerService.instance.client.GetAsync(requestPath);

            if (!response.IsSuccessStatusCode)
            {
                MessageBox.Show("Error while joining channel");
                return;
            }

            JObject responseJson = JObject.Parse(await response.Content.ReadAsStringAsync());

            if (!(responseJson.ContainsKey("drawPng") && responseJson.ContainsKey("drawPxl") && responseJson.ContainsKey("object")))
            {
                MessageBox.Show("Error parsing server response");
                return;
            }

            GeneratedImageStrokes = (JArray)responseJson.GetValue("drawPxl");
            Base64ImageData = responseJson.GetValue("drawPng").ToString().Split(',')[1];
            ObjectName = responseJson.GetValue("object").ToString();
            IsReqActive = true;
        }

        private void ChoseFile(object x)
        {
            OpenFileDialog op = new OpenFileDialog();
            op.Title = "Select a picture";
            op.Filter = "All supported graphics|*.bmp;*.jpg;*.png|PNG Files (*.png)|*.png|JPG Files (*.jpg)|*.jpg|BMP Files (*.bmp)|*.bmp";

            bool? result = op.ShowDialog();

            if (result == true)
                SelectedImage = new BitmapImage(new Uri(op.FileName));
        }

        #endregion

        #region Commands

        private ICommand _acceptCommand;
        public ICommand AcceptCommand
        {
            get
            {
                return _acceptCommand ?? (_acceptCommand = new RelayCommand(async x =>
                {
                    await Accept();
                }));
            }
        }

        private ICommand _cancelCommand;
        public ICommand CancelCommand
        {
            get
            {
                return _cancelCommand ?? (_cancelCommand = new RelayCommand(x =>
                {

                    JObject res = new JObject(new JProperty("IsAccept", false));
                    DialogHost.CloseDialogCommand.Execute(res, null);
                }));
            }
        }

        private ICommand _addHintCommand;
        public ICommand AddHintCommand
        {
            get
            {
                return _addHintCommand ?? (_addHintCommand = new RelayCommand(x =>
                {
                    Hints.Add(new HintModel(false));
                }));
            }
        }

        private ICommand _removeHintCommand;
        public ICommand RemoveHintCommand
        {
            get
            {
                return _removeHintCommand ?? (_removeHintCommand = new RelayCommand(RemoveHint));
            }
        }

        private ICommand _selectCreationTypeCommand;
        public ICommand SelectCreationTypeCommand
        {
            get
            {
                return _selectCreationTypeCommand ?? (_selectCreationTypeCommand = new RelayCommand(SelectCreationType));
            }
        }

        private ICommand _generateNewQuickdrawCommand;
        public ICommand GenerateNewQuickDrawCommand 
        {
            get 
            {
                return _generateNewQuickdrawCommand ?? (_generateNewQuickdrawCommand = new RelayCommand(GenerateQuickDraw));
            }
        }

        private ICommand _choseFileCommand;
        public ICommand ChoseFileCommand
        {
            get
            {
                return _choseFileCommand ?? (_choseFileCommand = new RelayCommand(ChoseFile));
            }
        }
        #endregion
    }
}
