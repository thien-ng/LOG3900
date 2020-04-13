using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;

namespace PolyPaint.VueModeles
{
    class MainWindowViewModel : BaseViewModel, IDisposable  
    {
        public MainWindowViewModel()
        {
            // Add available pages and set page
            PageViewModels[nameof(LoginViewModel)] = new LoginViewModel();

            CurrentPageViewModel = PageViewModels[nameof(LoginViewModel)];

            Mediator.Subscribe("GoToLoginScreen", OnGoToLoginScreen);
            Mediator.Subscribe("GoToRegisterScreen", OnGoToRegisterScreen);
            Mediator.Subscribe("GoToHomeScreen", OnGoToHomeScreen);
        }

        #region Attributes
        private IPageViewModel _currentPageViewModel;
        private Dictionary<string, IPageViewModel> _pageViewModels;

        public Dictionary<string, IPageViewModel> PageViewModels
        {
            get
            {
                if (_pageViewModels == null)
                    _pageViewModels = new Dictionary<string, IPageViewModel>();

                return _pageViewModels;
            }
        }

        public IPageViewModel CurrentPageViewModel
        {
            get
            {
                return _currentPageViewModel;
            }
            set
            {
                _currentPageViewModel = value;
                ProprieteModifiee("CurrentPageViewModel");
            }
        }
        #endregion

        #region Methods
        private void ChangeViewModel(string viewModelName, Type viewModelType)
        {
            if (!PageViewModels.ContainsKey(viewModelName))
                PageViewModels[viewModelName] = (IPageViewModel)Activator.CreateInstance(viewModelType);
            
            CurrentPageViewModel = PageViewModels[viewModelName];
        }

        private void OnGoToLoginScreen(object obj)
        {
            
            ChangeViewModel(nameof(LoginViewModel), typeof(LoginViewModel));
            if (PageViewModels.ContainsKey(nameof(HomeViewModel)) && PageViewModels[nameof(HomeViewModel)] != null)
                PageViewModels.Remove(nameof(HomeViewModel));
        }

        private void OnGoToRegisterScreen(object obj)
        {
            ChangeViewModel(nameof(RegisterViewModel), typeof(RegisterViewModel));
        }


        private void OnGoToHomeScreen(object obj)
        {
            ChangeViewModel(nameof(HomeViewModel), typeof(HomeViewModel));
            if (PageViewModels.ContainsKey(nameof(LoginViewModel)) && PageViewModels[nameof(LoginViewModel)] != null)
                PageViewModels.Remove(nameof(LoginViewModel));
        }

        public override void Dispose()
        {
            _pageViewModels = null;
        }
        #endregion
    }
}