package net.ghosttrails.www.mydetic;

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.ghosttrails.www.mydetic.api.FirebaseMemoryApi;

public class FirebaseLoginActivity extends FragmentActivity {
  // See: https://developer.android.com/training/basics/intents/result
  private final ActivityResultLauncher<Intent> signInLauncher =
      registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

  private FirebaseMemoryApi.FirebaseAction loginResultAction;
  private boolean pendingLogin = false;

  private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
    pendingLogin = false;
    IdpResponse response = result.getIdpResponse();
    if (result.getResultCode() == RESULT_OK) {
      // Successfully signed in
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      AppUtils.smallToast(this, "Firebase login succeeded: " + user.getEmail());
      if (loginResultAction != null) {
        loginResultAction.onAuthenticated(user);
        loginResultAction = null;
      }
    } else {
      // Sign in failed. If response is null the user canceled the
      // sign-in flow using the back button. Otherwise check
      // response.getError().getErrorCode() and handle the error.
      // ...
      AppUtils.smallToast(this, "Firebase login failed");
      if (loginResultAction != null) {
        loginResultAction.onAuthenticationError(response);
        loginResultAction = null;
      }
    }
  }

  public void launchFirebaseLogin(FirebaseMemoryApi.FirebaseAction action) {
    if (pendingLogin) {
      return;
    }
    // Choose authentication providers
    List<AuthUI.IdpConfig> providers =
            Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());

    // Create and launch sign-in intent
    Intent signInIntent =
        AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build();
    pendingLogin = true;
    loginResultAction = action;
    signInLauncher.launch(signInIntent);
  }

  public void signOutFirebase() {
    AuthUI.getInstance()
        .signOut(this)
        .addOnCompleteListener(
            task -> AppUtils.smallToast(FirebaseLoginActivity.this, "Firebase logged out"));
  }

  @Override
  protected void onStart() {
    super.onStart();
    MemoryAppState.getInstance().registerFirebaseLoginActivity(this);
  }

  @Override
  protected void onStop() {
    MemoryAppState.getInstance().unregisterFirebaseLoginActivity();
    loginResultAction = null;
    super.onStop();
  }
}
