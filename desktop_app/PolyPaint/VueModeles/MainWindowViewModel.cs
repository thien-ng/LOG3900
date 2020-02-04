using PolyPaint.Utilitaires;
using PolyPaint.VueModeles.Chat;
using System.Collections.Generic;
using System.Linq;
using static PolyPaint.Utilitaires.Constants;

namespace PolyPaint.VueModeles
{
    class MainWindowViewModel : VueModele
    {
        private IPageViewModel _currentPageViewModel;
        private List<IPageViewModel> _pageViewModels;

        public List<IPageViewModel> PageViewModels
        {
            get
            {
                if (_pageViewModels == null)
                    _pageViewModels = new List<IPageViewModel>();

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
            if (!PageViewModels.Contains(viewModel))
                PageViewModels.Add(viewModel);

            CurrentPageViewModel = PageViewModels
                .FirstOrDefault(vm => vm == viewModel);
        }

        private void OnGoToLoginScreen(object obj)
        {
            ChangeViewModel(PageViewModels[Constants.Vues.Login]);
        }

        private void OnGoToRegisterScreen(object obj)
        {
            ChangeViewModel(PageViewModels[Constants.Vues.Register]);
        }

        private void OnGoToDrawScreen(object obj)
        {
            ChangeViewModel(PageViewModels[Constants.Vues.Draw]);
        }

        private void OnGoToChatScreen(object obj)
        {
            ChangeViewModel(PageViewModels[Constants.Vues.Chat]);
        }
        
        public MainWindowViewModel()
        {
            // Add available pages and set page
            PageViewModels.Add(new LoginViewModel());
            PageViewModels.Add(new RegisterViewModel());
            PageViewModels.Add(new DessinViewModel());
            PageViewModels.Add(new MessageListViewModel());

            CurrentPageViewModel = PageViewModels[Constants.Vues.Chat];

            Mediator.Subscribe("GoToLoginScreen", OnGoToLoginScreen);
            Mediator.Subscribe("GoToDrawScreen", OnGoToDrawScreen);
            Mediator.Subscribe("GoToRegisterScreen", OnGoToRegisterScreen);
            Mediator.Subscribe("GoToChatScreen", OnGoToChatScreen);
        }

    }
}

