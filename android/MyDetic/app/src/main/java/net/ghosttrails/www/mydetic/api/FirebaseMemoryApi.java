package net.ghosttrails.www.mydetic.api;

import android.content.Context;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.time.LocalDate;
import net.ghosttrails.www.mydetic.AppUtils;
import net.ghosttrails.www.mydetic.FirebaseLoginActivity;
import net.ghosttrails.www.mydetic.MemoryAppState;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

public class FirebaseMemoryApi implements MemoryApi {

  public interface FirebaseAction {
    void doAction(FirebaseUser user);

    default void onError(Context context, IdpResponse response) {
      if (response == null) {
        AppUtils.smallToast(context, "User cancelled login flow");
      } else {
        FirebaseUiException error = response.getError();
        if (error != null) {
          AppUtils.smallToast(context, error.getMessage());
        } else {
          AppUtils.smallToast(context, "Null error code");
        }
      }
    }
  }

  @Override
  public void getMemories(String userId, MemoryListListener listener) {
    // TODO
    checkLogin(user -> listener.onApiResponse(new MemoryDataList()));
  }

  @Override
  public void getMemories(
      String userId, LocalDate fromDate, LocalDate toDate, MemoryListListener listener) {
    // TODO
    checkLogin(user -> listener.onApiResponse(new MemoryDataList()));
  }

  @Override
  public void getMemory(String userId, LocalDate memoryDate, SingleMemoryListener listener) {
    // TODO
    checkLogin(user -> listener.onApiError(new MyDeticException("Unimplemented")));
  }

  @Override
  public void putMemory(String userId, MemoryData memory, SingleMemoryListener listener) {
    // TODO
    checkLogin(user -> listener.onApiError(new MyDeticException("Unimplemented")));
  }

  @Override
  public void deleteMemory(String userId, LocalDate memoryDate, SingleMemoryListener listener) {
    // TODO
    checkLogin(user -> listener.onApiError(new MyDeticException("Unimplemented")));
  }

  private void checkLogin(FirebaseAction action) {
    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
      FirebaseLoginActivity firebaseLoginActivity = MemoryAppState.getInstance().getFirebaseLoginActivity();
      if (firebaseLoginActivity != null) {
        firebaseLoginActivity.launchFirebaseLogin(action);
      }
    } else {
      action.doAction(FirebaseAuth.getInstance().getCurrentUser());
    }
  }
}
