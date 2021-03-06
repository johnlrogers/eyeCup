package com.ora.android.eyecup.ui.login;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ora.android.eyecup.AlwaysService;
import com.ora.android.eyecup.R;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_EXPIRE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_OPEN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_WARN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_POLL;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_ERR_NO_ERR;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_NO_ERR;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    private TextView txtInstruction;
    private String mstrActTxt = "";

    private boolean isBound = false;
    private AlwaysService alwaysService;

    private int mAlwaysServiceState = ALWAYS_SVC_STATE_POLL;
    private int mPrevAlwaysServiceState = ALWAYS_SVC_STATE_POLL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        setContentView(R.layout.activity_login);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        KeyguardManager val = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        try {
            val.requestDismissKeyguard(this, null);
        } catch (NullPointerException e) {
            Log.e("Login:onCreate:requestDismissKeyguard:NPEx", e.toString());
        }

        txtInstruction = findViewById(R.id.txtInstruction);
        mstrActTxt =  getIntent().getStringExtra("ExpireTime");
        String strMsg;
        mAlwaysServiceState = getIntent().getIntExtra("EventWindowState", 0);
        if ((mAlwaysServiceState != mPrevAlwaysServiceState)
            || mAlwaysServiceState == ALWAYS_SVC_STATE_POLL ){
            switch (mAlwaysServiceState) {
                case ALWAYS_SVC_STATE_EVT_WIN_OPEN:
                    strMsg = "Your event window is open.  You have until ";
                    strMsg = strMsg + mstrActTxt;
                    strMsg = strMsg + " to perform this event.";
                    txtInstruction.setText(strMsg);
                    break;

                    case ALWAYS_SVC_STATE_EVT_WIN_WARN:
                    strMsg = "Warning: Your event window is closing.  You have until ";
                    strMsg = strMsg + mstrActTxt;
                    strMsg = strMsg + " to perform this event.";
                    txtInstruction.setText(strMsg);
                    break;

                case ALWAYS_SVC_STATE_EVT_WIN_EXPIRE:
                    strMsg = "Your event Window has closed.  You will be prompted when your next window is open.";
                    txtInstruction.setText(strMsg);
                    finish();
                    break;

                case ALWAYS_SVC_STATE_POLL:
                    if (!mstrActTxt.equals("")) {
                        txtInstruction.setText(mstrActTxt);
                    }
                    break;
                default:
                    txtInstruction.setText("");
                    break;
            }
        }

        //todo according to web, its a compiler issue; not depreceated.  Check again
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
//        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());

                if (isBound) {
                    alwaysService.stopPlaying();
                }
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
//JLR 20200126
//                    loginViewModel.login(usernameEditText.getText().toString(),
//                            passwordEditText.getText().toString());
                    TryLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Login:OnClickListener", "Login LoginListener");
                boolean bLogin = false;

                bLogin = TryLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                if (bLogin) {
                    try {
                        if (isBound) {
                            alwaysService.stopPlaying();
                        }
                        finish();
                    } catch (Exception e) {
                        Log.e("Login:OnClickListener:finish", e.toString());
                    }
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
//        stopPlaying();

    }
//    private void stopPlaying() {
//        if (mPlayer != null) {
//            mPlayer.stop();
//            mPlayer.release();
//            mPlayer = null;
//        }
//    }
//    private void playAlertSound(boolean bWarn) {
////        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
////        MediaPlayer thePlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//        stopPlaying();
//
//        try {
//            if (bWarn) {
//                Uri uriWarn = null;
//                RingtoneManager ringtoneMgr = new RingtoneManager(this);
//                ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
//                Cursor alarmsCursor = ringtoneMgr.getCursor();
//                int alarmsCount = alarmsCursor.getCount();
//                Uri[] alarms = new Uri[alarmsCount];
//                if (alarmsCount != 0 || alarmsCursor.moveToFirst()) {
//                    while(!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
//                        int currentPosition = alarmsCursor.getPosition();
//                        alarms[currentPosition] = ringtoneMgr.getRingtoneUri(currentPosition);
//                        if (currentPosition == 12) {
//                            uriWarn = alarms[currentPosition];
//                        }
//                    }
//                    alarmsCursor.close();
//                }
//                if (uriWarn != null) {
//                    mPlayer = MediaPlayer.create(getApplicationContext(), uriWarn);
//                }
//            } else {
//                RingtoneManager notifyMgr = new RingtoneManager(this);
//                notifyMgr.setType(TYPE_NOTIFICATION);
//                Cursor notifyCursor = notifyMgr.getCursor();
//                int notifyCount = notifyCursor.getCount();
//                Uri[] notifications = new Uri[notifyCount];
//                if (notifyCount != 0 || notifyCursor.moveToFirst()) {
//                    while(!notifyCursor.isAfterLast() && notifyCursor.moveToNext()) {
//                        int currentPosition = notifyCursor.getPosition();
//                        notifications[currentPosition] = notifyMgr.getRingtoneUri(currentPosition);
////                Log.i(notifications[currentPosition].);
//                    }
//                    notifyCursor.close();
//                }
////            Uri uri = notifyMgr.getRingtoneUri(12);
//                mPlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), TYPE_NOTIFICATION));
//            }
//            mPlayer.start();
//
//        } catch (Exception e) {
//            Log.e("Login:playAlertSound", e.toString());
//        }
//    }

    private boolean TryLogin(String strUser, String strPW) {
        boolean bLogin = false;
        int iLoginState = alwaysService.TryLogin(strUser, strPW);
        if (iLoginState == LOGIN_PARTICPANT_ERR_NO_ERR) {
//            alwaysService.setServiceEventState(LOGIN_PARTICPANT_ERR_NO_ERR);
            alwaysService.setLoginEvtState(LOGIN_PARTICPANT_ERR_NO_ERR);
            bLogin = true;
        }
//20200211
        if (iLoginState == LOGIN_ADMIN_ERR_NO_ERR) {
//            alwaysService.setServiceEventState(LOGIN_ADMIN_ERR_NO_ERR);
            alwaysService.setLoginEvtState(LOGIN_ADMIN_ERR_NO_ERR);
            bLogin = true;
        }
//20200211 end
        loginViewModel.loginMessage(iLoginState);
        return bLogin;
    }

    private void updateUiWithUser(LoggedInUserView model) {

        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (isBound) {
            int iRet = alwaysService.StartEvent();
        }

        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AlwaysService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
//        // Unregister since the activity is about to be closed.
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();

        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AlwaysService.LocalBinder binder = (AlwaysService.LocalBinder) service;
            alwaysService = binder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
}
