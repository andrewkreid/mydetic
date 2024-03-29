package net.ghosttrails.www.mydetic;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import java.time.LocalDate;

/** Adaptor to display memory cards for the last NUM_CARDS days. */
class MemoryCardviewAdapter extends RecyclerView.Adapter<MemoryCardviewAdapter.ViewHolder> {

  // How many days from the ideal point in the past (eg "1 year ago") we search for a memory.
  private static final int DAY_THRESH = 12;

  private int cardHistoryType;

  private CustomItemClickListener listener;

  // List of calculated "the past" memory dates.
  private ArrayList<LocalDate> pastMemoryDates;

  // Provide a suitable constructor (depends on the kind of dataset)
  MemoryCardviewAdapter(CustomItemClickListener listener) {
    this.listener = listener;
    this.cardHistoryType = MyDeticConfig.LISTSETTING_THISWEEK;
    this.pastMemoryDates = new ArrayList<>();
  }

  // Create new views (invoked by the layout manager)
  @Override
  public MemoryCardviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // create a new view
    CardView v =
        (CardView)
            LayoutInflater.from(parent.getContext()).inflate(R.layout.memory_card, parent, false);
    int errorColor = parent.getContext().getResources().getColor(R.color.error_text);
    int memoryTextColor = parent.getContext().getResources().getColor(R.color.memory_card_text);
    final ViewHolder mViewHolder = new ViewHolder(v, errorColor, memoryTextColor);
    v.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            listener.onItemClick(v, mViewHolder.getLayoutPosition(), mViewHolder.memoryDate);
          }
        });
    // TODO set the view's size, margins, paddings and layout parameters
    return mViewHolder;
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    LocalDate memoryDate = positionToDate(position);
    MemoryAppState appState = MemoryAppState.getInstance();
    MemoryData memory = appState.getCachedMemory(memoryDate);
    if (memory != null) {
      holder.fillCard(memory);
    } else {
      // If the date is in the list but we don't have the memory in the cache, attempt
      // to download it.
      if (!appState.getMemories().hasDate(memoryDate)) {
        loadSingleMemoryFromApi(memoryDate);
      }
      holder.fillCard(memoryDate);
    }
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    switch (cardHistoryType) {
      case MyDeticConfig.LISTSETTING_THISWEEK:
        return 7; // previous week.
      case MyDeticConfig.LISTSETTING_THEPAST:
        return pastMemoryDates.size();
      default:
        return 0;
    }
  }

  /**
   * Map a list position to a date. Position 0 is today, 1 is yesterday, and so on...
   *
   * @param position the list position.
   */
  private LocalDate positionToDate(int position) {
    switch (cardHistoryType) {
      case MyDeticConfig.LISTSETTING_THISWEEK:
        return positionToDateForThisWeek(position);
      case MyDeticConfig.LISTSETTING_THEPAST:
        return positionToDateForThePast(position);
      default:
        Log.e("MemoryCardviewAdapter", "unrecognised cardHistoryType");
        return LocalDate.now();
    }
  }

  private LocalDate positionToDateForThisWeek(int position) {
    try {
      LocalDate today = LocalDate.now(ZoneId.systemDefault());
      return today.minusDays(position);
    } catch (IllegalArgumentException e) {
      Log.e("MemoryCardviewAdapter", String.format("Error setting timezone (%s)", e.toString()));
      // Fall back to default TZ (UTC)
      return LocalDate.now().minusDays(position);
    }
  }

  private LocalDate positionToDateForThePast(int position) {
    return pastMemoryDates.get(position);
  }

  void setCardHistoryType(int cardHistoryType) {
    this.cardHistoryType = cardHistoryType;
    if (this.cardHistoryType == MyDeticConfig.LISTSETTING_THEPAST) {
      recalculatePastMemoryDates();
    }
  }

  /**
   * Fill the pasMemoryDates array with the dates of existing memories from various points in the
   * past.
   *
   * <p>Ideally we want: today yesterday 1 week ago 1 month ago 1 year ago 2 years ago 3 years ago 4
   * years ago 5 years ago
   *
   * <p>In each case, pick the nearest memory, but don't add a date more than once. Add today and
   * yesterday even if there is no memory for those dates.
   */
  void recalculatePastMemoryDates() {
    pastMemoryDates.clear();

    LocalDate today = positionToDateForThisWeek(0);
    pastMemoryDates.add(today);
    pastMemoryDates.add(today.minusDays(1));

    findAndAddNearestMemoryDate(today.minusDays(7));
    findAndAddNearestMemoryDate(today.minusMonths(1));
    for (int i = 1; i <= 15; i++) {
      findAndAddNearestMemoryDate(today.minusYears(i));
    }

    // Download any uncached memories in the list
    final MemoryAppState appState = MemoryAppState.getInstance();
    for (LocalDate memoryDate : pastMemoryDates) {
      if (appState.getCachedMemory(memoryDate) == null) {
        loadSingleMemoryFromApi(memoryDate);
      }
    }
  }

  private void loadSingleMemoryFromApi(LocalDate memoryDate) {
    final MemoryAppState appState = MemoryAppState.getInstance();
    appState
        .getApi()
        .getMemory(
            appState.getConfig().getUserName(),
            memoryDate,
            new MemoryApi.SingleMemoryListener() {
              @Override
              public void onApiResponse(MemoryData memory) {
                try {
                  appState.setCachedMemory(memory);
                  notifyDataSetChanged();
                } catch (MyDeticException e) {
                  Log.e("MemoryCardAdaptor", e.getMessage());
                }
              }

              @Override
              public void onApiError(MyDeticException exception) {
                Log.e("MemoryCardAdaptor", exception.getMessage());
              }
            });
  }

  private void findAndAddNearestMemoryDate(LocalDate date) {
    Set<LocalDate> memoryDates = MemoryAppState.getInstance().getMemories().getDates();
    LocalDate foundDate = null;
    // search around the provided date up to DAY_THRESH days
    for (int i = 0; i <= DAY_THRESH; i++) {
      LocalDate candidateDate = date.minusDays(i);
      if (memoryDates.contains(candidateDate)) {
        foundDate = candidateDate;
        break;
      }
      candidateDate = date.plusDays(i);
      if (memoryDates.contains(candidateDate)) {
        foundDate = candidateDate;
        break;
      }
    }
    if ((foundDate != null) && !pastMemoryDates.contains(foundDate)) {
      pastMemoryDates.add(foundDate);
    }
  }

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  static class ViewHolder extends RecyclerView.ViewHolder {
    public LocalDate memoryDate;
    CardView mView;
    TextView titleView;
    TextView descriptionView;
    TextView memoryTextView;
    private int mErrorColor;
    private int mMemoryTextColor;

    ViewHolder(CardView v, int errorColor, int textColor) {
      super(v);
      mView = v;
      mErrorColor = errorColor;
      mMemoryTextColor = textColor;
      titleView = mView.findViewById(R.id.memory_card_view_title);
      memoryTextView = mView.findViewById(R.id.memory_card_text_view);
      descriptionView = mView.findViewById(R.id.memory_card_view_description);
    }

    void fillCard(LocalDate d) {
      memoryDate = d;
      titleView.setText(Utils.isoFormatWithDay(d));
      memoryTextView.setText("");
      descriptionView.setText(descriptionForDate(memoryDate));
    }

    void fillCard(MemoryData memory) {
      memoryDate = memory.getMemoryDate();
      titleView.setText(Utils.isoFormatWithDay(memory.getMemoryDate()));
      memoryTextView.setText(memory.getMemoryText());
      if (memory.getCacheState() == MemoryData.CACHESTATE_PENDING_SAVE) {
        descriptionView.setText(R.string.memory_unsaved);
        descriptionView.setTextColor(mErrorColor);
      } else {
        descriptionView.setText(descriptionForDate(memoryDate));
        descriptionView.setTextColor(mMemoryTextColor);
      }
    }

    /**
     * Return a text description for a date, relative to today. Returns things like "Today",
     * "Yesterday", "Last Week", "2 Years Ago" etc.
     *
     * @param d The Date to get a description for.
     * @return A description for the date, or an empty string if no description applies.
     */
    private String descriptionForDate(LocalDate d) {
      LocalDate nowDate = LocalDate.now();
      long dayDiff = DAYS.between(d, nowDate);
      long monthDiff = MONTHS.between(d, nowDate);
      // int yearDiff = Years.yearsBetween(d, nowDate).getYears();

      // Sort of round to nearest, so that dates that are a few days less that a year ago
      // still say "1 year ago" etc.
      int yearDiff = (int) Math.floor(dayDiff / 365.0 + 0.2);
      long weekDiff = WEEKS.between(d, nowDate);
      if (yearDiff > 1) {
        return String.format(Locale.getDefault(), "%d years ago", yearDiff);
      } else if (monthDiff > 2) {
        return String.format(Locale.getDefault(), "%s months ago", monthDiff);
      } else if (weekDiff > 1) {
        return String.format(Locale.getDefault(), "%s weeks ago", weekDiff);
      } else {
        if (dayDiff == 0) {
          return "Today";
        } else if (dayDiff == 1) {
          return "Yesterday";
        } else if (dayDiff == 7) {
          return "1 week ago";
        } else {
          return String.format(Locale.getDefault(), "%d days ago", dayDiff);
        }
      }
    }
  }
}
