using PolyPaint.Services;
using PolyPaint.Utilitaires;
using Quobject.SocketIoClientDotNet.Client;
using System;
using System.Collections.Generic;

namespace PolyPaint.VueModeles
{
    class MainWindowViewModel : BaseViewModel
    {
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

        private void ChangeViewModel(string viewModelName, Type viewModelType)
        {
            if (!PageViewModels.ContainsKey(viewModelName))
                PageViewModels[viewModelName] = (IPageViewModel)Activator.CreateInstance(viewModelType);
            
            CurrentPageViewModel = PageViewModels[viewModelName];
        }

        private void OnGoToLoginScreen(object obj)
        {
            
            ChangeViewModel(nameof(LoginViewModel), typeof(LoginViewModel));
        }

        private void OnGoToRegisterScreen(object obj)
        {
            ChangeViewModel(nameof(RegisterViewModel), typeof(RegisterViewModel));
        }

        private void OnGoToDrawScreen(object obj)
        {
            ChangeViewModel(nameof(DessinViewModel), typeof(DessinViewModel));
        }

        private void OnGoToHomeScreen(object obj)
        {
            ChangeViewModel(nameof(HomeViewModel), typeof(HomeViewModel));
        }

        public MainWindowViewModel()
        {
            // Add available pages and set page
            PageViewModels[nameof(LoginViewModel)] = new LoginViewModel();

            CurrentPageViewModel = PageViewModels[nameof(LoginViewModel)];

            Mediator.Subscribe("GoToLoginScreen", OnGoToLoginScreen);
            Mediator.Subscribe("GoToDrawScreen", OnGoToDrawScreen);
            Mediator.Subscribe("GoToRegisterScreen", OnGoToRegisterScreen);
            Mediator.Subscribe("GoToHomeScreen", OnGoToHomeScreen);

            Socket socket = IO.Socket(Constants.SERVER_PATH);
            socket.On(Socket.EVENT_CONNECT, () =>
            {
                ServerService.instance.socket = socket;
            });
        }

    }
}