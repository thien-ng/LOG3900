
using MaterialDesignThemes.Wpf;
using Newtonsoft.Json.Linq;
using PolyPaint.Modeles;
using PolyPaint.Services;
using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Input;

namespace PolyPaint.VueModeles
{
    class CreateGameViewModel: BaseViewModel
    {

        public CreateGameViewModel()
        {
            Difficulty = new ObservableCollection<string> { "Easy", "Medium", "Hard" };
            DisplayMode = new ObservableCollection<string> { "Classic", "Random", "Panoramic", "Centered" };
            Hints = new ObservableCollection<HintModel> { new HintModel(true) };
            DrawViewModel = new CreateGameDrawViewModel();
            SelectedCreationType = CreationType.Manual;
            SelectedDisplayMode = DisplayMode[0];
            SelectedDifficulty = Difficulty[1];
            IsReqActive = true;
        }


        #region Public Attributes

        public ObservableCollection<string> Difficulty { get; set; }
        public ObservableCollection<string> DisplayMode { get; set; }
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

        #endregion

        #region Commands

        private ICommand _acceptCommand;
        public ICommand AcceptCommand
        {
            get
            {
                return _acceptCommand ?? (_acceptCommand = new RelayCommand(x =>
                {
                    JArray drawing = DrawViewModel.GetDrawing();
                    List<string> clues = new List<string>();

                    foreach (var hint in Hints)
                        clues.Add(hint.Hint);

                    var newGame = new JObject( new JProperty("solution", Solution),
                                               new JProperty("clues", clues.ToArray()), 
                                               new JProperty("difficulty", SelectedDifficulty.ToLower()), 
                                               new JProperty("drawing", drawing),
                                               new JProperty("displayMode", SelectedDisplayMode)); 
                    
                    // Do post here //

                    JObject res = new JObject(new JProperty("IsAccept", true));
                    DialogHost.CloseDialogCommand.Execute(res, null);

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
                return _removeHintCommand ?? (_removeHintCommand = new RelayCommand(x =>
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
                }));
            }
        }

        private ICommand _selectCreationTypeCommand;
        public ICommand SelectCreationTypeCommand
        {
            get
            {
                return _selectCreationTypeCommand ?? (_selectCreationTypeCommand = new RelayCommand(x =>
                {
                    string creationType = (string) x;

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
                }));
            }
        }

        private ICommand _generateNewQuickdrawCommand;
        public ICommand GenerateNewQuickDrawCommand 
        {
            get 
            {
                return _generateNewQuickdrawCommand ?? (_generateNewQuickdrawCommand = new RelayCommand(async x =>
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

                    // TODO garder en memoire drawpxl
                    Base64ImageData = responseJson.GetValue("drawPng").ToString().Split(',')[1];
                    ObjectName = responseJson.GetValue("object").ToString();
                    IsReqActive = true;
                }));
            }
        }
        #endregion
    }
}
