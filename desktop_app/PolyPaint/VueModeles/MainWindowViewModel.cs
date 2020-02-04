using PolyPaint.Services;
using PolyPaint.Utilitaires;
using PolyPaint.VueModeles.Chat;
using Quobject.SocketIoClientDotNet.Client;
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

        private void ChangeViewModel(IPageViewModel viewModel)
        {
            if (!PageViewModels.ContainsKey(nameof(viewModel)))
                PageViewModels[nameof(viewModel)] = viewModel;

            CurrentPageViewModel = PageViewModels[nameof(viewModel)];
        }

        private void OnGoToLoginScreen(object obj)
        {
            ChangeViewModel(new LoginViewModel());
        }

        private void OnGoToRegisterScreen(object obj)
        {
            ChangeViewModel(new RegisterViewModel());
        }

        private void OnGoToDrawScreen(object obj)
        {
            ChangeViewModel(new DessinViewModel());
        }

        private void OnGoToChatScreen(object obj)
        {
            ChangeViewModel(new MessageListViewModel());
        }

        public MainWindowViewModel()
        {
            // Add available pages and set page
            PageViewModels[nameof(LoginViewModel)] = new LoginViewModel();

            CurrentPageViewModel = PageViewModels[nameof(LoginViewModel)];

            Mediator.Subscribe("GoToLoginScreen", OnGoToLoginScreen);
            Mediator.Subscribe("GoToDrawScreen", OnGoToDrawScreen);
            Mediator.Subscribe("GoToRegisterScreen", OnGoToRegisterScreen);
            Mediator.Subscribe("GoToChatScreen", OnGoToChatScreen);

            Socket socket = IO.Socket(Constants.SERVER_PATH);
            socket.On(Socket.EVENT_CONNECT, () =>
            {
                //Console.WriteLine("connect");
                ServerService.instance.socket = socket;
            });
        }

    }
}