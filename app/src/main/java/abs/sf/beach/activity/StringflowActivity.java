package abs.sf.beach.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.nio.charset.IllegalCharsetNameException;

import abs.ixi.client.Platform;
import abs.ixi.client.core.Callback;
import abs.ixi.client.core.InitializationErrorException;
import abs.ixi.client.io.StreamNegotiator;
import abs.ixi.client.net.NetworkException;
import abs.ixi.client.util.TaskExecutor;
import abs.sf.beach.utils.AndroidUtils;
import abs.sf.beach.utils.ApplicationProps;
import abs.sf.beach.utils.SharedPrefs;
import abs.sf.client.android.managers.AndroidUserManager;
import abs.sf.client.android.utils.AndroidSdkInitializer;
import abs.sf.client.android.utils.ContextProvider;

/**
 * Root activity for all the activities defined in Beach application.
 */
public abstract class StringflowActivity extends AppCompatActivity implements ContextProvider {

    /**
     * A common progress dialog for all the activities defined in Beach application
     *
     * @return
     */
    protected ProgressDialog getProgressDialog(String msg) {
        return getProgressDialog(msg, false);
    }

    /**
     * A common progress dialog for all the activities defined in Beach application
     *
     * @return
     */
    protected ProgressDialog getProgressDialog(String msg, boolean cancelable) {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage(msg);
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(cancelable);

        return pDialog;
    }

    protected void closeProgressDialog(ProgressDialog pDialog) {
        if (pDialog != null) {
            pDialog.dismiss();
        }
    }

    protected boolean initilizeSDK() {
        try {
            return Platform.initialize(new AndroidSdkInitializer(ApplicationProps.XMPP_SERVER, ApplicationProps.XMPP_SERVER_PORT,
                    ApplicationProps.MEDIA_SERVER, ApplicationProps.MEDIA_SERVER_PORT, this));

        } catch (InitializationErrorException e) {
            AndroidUtils.showToast(this, "Stringflow SDK initiliziation failed, Please restart App");
            finish();
        }

        return false;
    }

    protected void loginBackground() {
        if (SharedPrefs.getInstance().getLoginStatus()) {
            if (!Platform.getInstance().isUserAuthenticated()) {
                TaskExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        while (!Platform.getInstance().isUserAuthenticated()) {
                            try {
                                StreamNegotiator.NegotiationResult result = Platform.getInstance().login(SharedPrefs.getInstance().getUsername(), ApplicationProps.DOMAIN,
                                        SharedPrefs.getInstance().getPassword());

                                if (result.isError()) {
                                    System.out.println("Waiting for a second during background login");
                                    waitForASec();
                                }

                            } catch (NetworkException e) {
                                System.out.println("Network Exception while try for background login : " + e.getReason());
                                waitForASec();
                            }
                        }
                    }
                });

            } else {
                System.out.println("Already logged in");
            }

        } else {
            AndroidUtils.showToast(this, "Login first");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void waitForASec() {
        try {
            System.out.println("Waiting for a second during background login");
            Thread.sleep(1000);

        } catch (InterruptedException e) {
            //swellow exception
        }
    }

    protected void logout() {

        final ProgressDialog progressDialog = getProgressDialog("Logout in progress...");
        progressDialog.show();

        TaskExecutor.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                final AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
                userManager.logoutUserAsync(new Callback<String, Exception>() {
                    @Override
                    public void onSuccess(String s) {
                        SharedPrefs.getInstance().clear();
                        closeProgressDialog(progressDialog);
                        startActivity(new Intent(context(), LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AndroidUtils.showToast(context(), "Something went wrong, Please restart your app");
                            }
                        });

                        closeProgressDialog(progressDialog);
                        SharedPrefs.getInstance().clear();
                        startActivity(new Intent(context(), LoginActivity.class));
                        finish();
                    }
                });
            }
        });

    }

    @Override
    public Context context() {
        return this;
    }
}
