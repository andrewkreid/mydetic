package net.ghosttrails.www.mydetic.api;

import androidx.annotation.NonNull;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import net.ghosttrails.www.mydetic.FirebaseLoginActivity;
import net.ghosttrails.www.mydetic.MemoryAppState;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

public class FirebaseMemoryApi implements MemoryApi {

  public abstract static class FirebaseAction {
    abstract void doAction(FirebaseUser user, DocumentSnapshot userDocument);

    abstract void onError(String message);

    public void onAuthenticated(FirebaseUser user) {
      getUserDocument(user, true);
    }

    private void getUserDocument(FirebaseUser user, boolean createIfRequired) {
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      DocumentReference docRef = db.collection("users").document(user.getUid());

      docRef
          .get()
          .addOnCompleteListener(
              new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                  if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                      doAction(user, document);
                    } else {
                      if (!createIfRequired) {
                        onError("Failed to create User Document");
                        return;
                      }
                      Map<String, Object> userData = new HashMap<>();
                      userData.put("uid", user.getUid());
                      userData.put("email", user.getEmail());
                      userData.put("name", user.getDisplayName());

                      db.collection("users")
                          .document(user.getUid())
                          .set(userData)
                          .addOnSuccessListener(
                              new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                  getUserDocument(user, false);
                                }
                              })
                          .addOnFailureListener(
                              new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                  onError("Error writing User document");
                                }
                              });
                    }
                  } else {
                    onError(
                        task.getException() != null
                            ? task.getException().getMessage()
                            : "Null error");
                  }
                }
              });
    }

    public void onAuthenticationError(IdpResponse response) {
      if (response == null) {
        onError("User cancelled login flow");
      } else {
        FirebaseUiException error = response.getError();
        if (error != null) {
          onError(error.getMessage());
        } else {
          onError("Null error code");
        }
      }
    }
  }

  @Override
  public void getMemories(String userId, MemoryListListener listener) {
    // TODO
    listener.onApiError(new MyDeticException("Unimplemented"));
  }

  @Override
  public void getMemories(
      String userId, LocalDate fromDate, LocalDate toDate, MemoryListListener listener) {
    // TODO
    listener.onApiError(new MyDeticException("Unimplemented"));
  }

  @Override
  public void getMemory(String userId, LocalDate memoryDate, SingleMemoryGetListener listener) {
    // TODO
    listener.onApiGetError(new MyDeticException("Unimplemented"));
  }

  @Override
  public void putMemory(String userId, MemoryData memory, SingleMemoryPutListener listener) {
    // TODO
    checkLogin(
        new FirebaseAction() {
          @Override
          void doAction(FirebaseUser user, DocumentSnapshot userDocument) {
            Map<String, Object> memoryData = new HashMap<>();
            memoryData.put("date", Utils.isoFormat(memory.getMemoryDate()));
            memoryData.put("memoryText", memory.getMemoryText());
            // TODO: Store and update revisions, or maybe last updated time?
            userDocument
                .getReference()
                .collection("memories")
                .document(Utils.isoFormat(memory.getMemoryDate()))
                .set(memoryData)
                .addOnSuccessListener(unused -> listener.onApiPutResponse(memory))
                .addOnFailureListener(
                    e -> listener.onApiPutError(new MyDeticException("Put Memory Failure", e)));
          }

          @Override
          void onError(String message) {
            listener.onApiPutError(new MyDeticException(message));
          }
        });
  }

  @Override
  public void deleteMemory(String userId, LocalDate memoryDate, SingleMemoryPutListener listener) {
    // TODO
    listener.onApiPutError(new MyDeticException("Unimplemented"));
  }

  private void checkLogin(FirebaseAction action) {
    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
      FirebaseLoginActivity firebaseLoginActivity =
          MemoryAppState.getInstance().getFirebaseLoginActivity();
      if (firebaseLoginActivity != null) {
        firebaseLoginActivity.launchFirebaseLogin(action);
      }
    } else {
      action.onAuthenticated(FirebaseAuth.getInstance().getCurrentUser());
    }
  }
}
