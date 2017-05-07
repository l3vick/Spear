package Fragments.register;

import Interactors.register.RegisterInteractorImp;
import Interactors.register.RegisterInteractor;

/**
 * Created by pablo.rojas on 5/5/17.
 */

public class RegisterPresenter {
  private RegisterInteractor registerInteractor;
  private final RegisterView view;

  public RegisterPresenter(RegisterView view) {
    this.view = view;
    this.registerInteractor = new RegisterInteractorImp(onRegisterCallback);
  }

  public void registerUser(String user, String mail, String password, String province) {
    view.showLoading();
    registerInteractor.registerUser(user, mail, password, province);
  }

  private final RegisterInteractor.OnRegisterCallback onRegisterCallback =
      new RegisterInteractor.OnRegisterCallback() {
        @Override public void onSuccess() {
          view.hideLoading();
          view.navigateToLogin();
        }

        @Override public void onError() {
          view.hideLoading();
          view.showError("error");
        }

      };
}
