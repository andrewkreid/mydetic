package net.ghosttrails.www.mydetic.api;

import android.util.Log;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import net.ghosttrails.www.mydetic.MemoryAppState;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FirebaseMemoryLister {

  // Max number of documents to request per query.
  public static final int QUERY_LIMIT = 250;

  private final String userId;
  private final FirebaseUser user;
  private final DocumentSnapshot userDocument;
  private final @Nullable LocalDate fromDate;
  private final @Nullable LocalDate toDate;

  public FirebaseMemoryLister(
      String userId,
      FirebaseUser user,
      DocumentSnapshot userDocument,
      @Nullable LocalDate fromDate,
      @Nullable LocalDate toDate) {
    this.userId = userId;
    this.user = user;
    this.userDocument = userDocument;
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  void listMemories(MemoryApi.MemoryListListener listener) {
    fetchFirstPage(listener);
  }

  private void fetchFirstPage(MemoryApi.MemoryListListener listener) {
    fetchNextPage(new ArrayList<>(), null, listener);
  }

  private void fetchNextPage(
      List<MemoryData> memories,
      @Nullable DocumentSnapshot lastVisible,
      MemoryApi.MemoryListListener listener) {
    Log.i(
        "MyDetic",
        String.format(
            "VVV: fetchNextPage: %d, %s",
            memories.size(), lastVisible == null ? "null" : lastVisible.getString("date")));
    Query next =
        userDocument.getReference().collection("memories").orderBy("date").limit(QUERY_LIMIT);
    if (lastVisible != null) {
      next = next.startAfter(lastVisible);
    }

    next.get()
        .addOnSuccessListener(
            queryDocumentSnapshots -> {
              if (queryDocumentSnapshots.isEmpty()) {
                // We're at the end of the collection.
                completeList(memories, listener);
              }

              try {
                // Add the documents we have and request the next batch.
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                  memories.add(memoryDataFromDocument(documentSnapshot));
                }

                fetchNextPage(
                    memories,
                    queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1),
                    listener);
              } catch (Exception e) {
                listener.onApiError(new MyDeticException(e.getMessage()));
              }
            })
        .addOnFailureListener(e -> listener.onApiError(new MyDeticException(e.getMessage())));
  }

  private void completeList(List<MemoryData> memories, MemoryApi.MemoryListListener listener) {
    // TODO: Might as well cache the memories here as we've gone to the trouble of downloading them.
    MemoryAppState appState = MemoryAppState.getInstance();

    MemoryDataList memoryList = new MemoryDataList(userId);
    for (MemoryData memoryData : memories) {
      LocalDate memoryDate = memoryData.getMemoryDate();
      if ((fromDate != null && memoryDate.isBefore(fromDate))
          || (toDate != null && memoryDate.isAfter(toDate))) {
        continue;
      }
      try {
        appState.setCachedMemory(memoryData);
      } catch (MyDeticException e) {
        Log.e("MyDetic", "VVV: failed to cache " + e.getMessage());
      }
      memoryList.setDate(memoryDate);
    }
    listener.onApiResponse(memoryList);
  }

  private MemoryData memoryDataFromDocument(DocumentSnapshot doc) {
    LocalDate date = Utils.parseIsoDate(doc.getString("date"));
    String memoryText = doc.getString("memoryText");

    return new MemoryData(userId, memoryText, date);
  }
}
