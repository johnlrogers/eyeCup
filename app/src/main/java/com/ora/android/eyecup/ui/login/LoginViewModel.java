package com.ora.android.eyecup.ui.login;

import com.ora.android.eyecup.R;
import com.ora.android.eyecup.data.LoginRepository;
import com.ora.android.eyecup.data.Result;
import com.ora.android.eyecup.data.model.LoggedInUser;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_ERR_PW;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_ID;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_PW;
import static com.ora.android.eyecup.Globals.MIN_PASSWORD_LEN;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            //todo flow/format of logic
//            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
//            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    public void loginMessage(int iLoginError) {
        switch (iLoginError) {
            case LOGIN_PARTICPANT_ERR_ID:
                loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
                break;
            case LOGIN_PARTICPANT_ERR_PW:
                loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
                break;
            case LOGIN_ADMIN_ERR_PW:
                loginFormState.setValue(new LoginFormState(null, R.string.invalid_admin_password));
                break;
            default:
                loginFormState.setValue(new LoginFormState(true));
                break;
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
//20200126 JLR
        return true;
//        if (username.contains("@")) {
//            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
//        } else {
//            return !username.trim().isEmpty();
//        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() >= MIN_PASSWORD_LEN;
    }

}
