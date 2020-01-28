using PolyPaint.Utilitaires;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.VueModeles
{
    class MainWindowViewModel : VueModele
    {
        private IPageViewModel _currentPageViewModel;
        private List<IPageViewModel> _pageViewModels;

        public List<Utilitaires.IPageViewModel> PageViewModels
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
            ChangeViewModel(PageViewModels[0]);
        }

        private void OnGoToRegisterScreen(object obj)
        {
            ChangeViewModel(PageViewModels[2]);
        }

        private void OnGoToDrawScreen(object obj)
        {
            ChangeViewModel(PageViewModels[1]);
        }

        public MainWindowViewModel()
        {
            // Add available pages and set page
            PageViewModels.Add(new LoginViewModel());
            PageViewModels.Add(new DessinViewModel());
            PageViewModels.Add(new RegisterViewModel());

            CurrentPageViewModel = PageViewModels[0];

            Mediator.Subscribe("GoToLoginScreen", OnGoToLoginScreen);
            Mediator.Subscribe("GoToDrawScreen", OnGoToDrawScreen);
            Mediator.Subscribe("GoToRegisterScreen", OnGoToRegisterScreen);
        }
    }
}

