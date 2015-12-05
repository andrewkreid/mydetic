package net.ghosttrails.www.mydetic;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;

import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Adaptor to display memory cards for the last NUM_CARDS days.
 */
public class MemoryCardviewAdaptor extends
    RecyclerView.Adapter<MemoryCardviewAdaptor.ViewHolder> {

  // How many days into the past to show days for.
  static final int NUM_CARDS = 7;

  private CustomItemClickListener listener;

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class ViewHolder extends RecyclerView.ViewHolder {
    public CardView mView;
    public TextView titleView;
    public TextView descriptionView;
    public TextView memoryTextView;
    public LocalDate memoryDate;

    public ViewHolder(CardView v) {
      super(v);
      mView = v;
      titleView = (TextView) mView.findViewById(R.id.memory_card_view_title);
      memoryTextView = (TextView) mView.findViewById(R.id.memory_card_text_view);
      descriptionView = (TextView) mView.findViewById(R.id.memory_card_view_description);
    }

    public void fillCard(LocalDate d) {
      memoryDate = d;
      titleView.setText(Utils.isoFormatWithDay(d));
      memoryTextView.setText("");
      descriptionView.setText(descriptionForDate(memoryDate));
    }

    public void fillCard(MemoryData memory) {
      memoryDate = memory.getMemoryDate();
      titleView.setText(Utils.isoFormatWithDay(memory.getMemoryDate()));
      memoryTextView.setText(memory.getMemoryText());
      descriptionView.setText(descriptionForDate(memoryDate));
    }

    /**
     * Return a text description for a date, relative to today. Returns things like
     * "Today", "Yesterday", "Last Week", "2 Years Ago" etc.
     * @param d The Date to get a description for.
     * @return A description for the date, or an empty string if no description applies.
     */
    private String descriptionForDate(LocalDate d) {
      LocalDate nowDate = LocalDate.now();
      int dayDiff = Days.daysBetween(d, nowDate).getDays();
      int monthDiff = Months.monthsBetween(d, nowDate).getMonths();
      int yearDiff = Years.yearsBetween(d, nowDate).getYears();
      int weekDiff = Weeks.weeksBetween(d, nowDate).getWeeks();
      if (yearDiff > 1) {
        return String.format("%d years ago", yearDiff);
      } else if (monthDiff > 2) {
        return String.format("%s months ago", monthDiff);
      } else if (weekDiff > 1) {
        return String.format("%s weeks ago", weekDiff);
      } else {
        if (dayDiff == 0) {
          return "Today";
        } else if (dayDiff == 1) {
          return "Yesterday";
        } else {
          return "";
        }
      }
    }

  }

  // Provide a suitable constructor (depends on the kind of dataset)
  public MemoryCardviewAdaptor(CustomItemClickListener listener) {
    this.listener = listener;
  }

  // Create new views (invoked by the layout manager)
  @Override
  public MemoryCardviewAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // create a new view
    CardView v = (CardView) LayoutInflater.from(parent.getContext())
        .inflate(R.layout.memory_card, parent, false);
    final ViewHolder mViewHolder = new ViewHolder(v);
    v.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClick(v, mViewHolder.getPosition(), mViewHolder.memoryDate);
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
      holder.fillCard(memoryDate);
    }
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    return NUM_CARDS;
  }

  /**
   * Map a list position to a date. Position 0 is today, 1 is yesterday, and
   * so on...
   *
   * @param position the list position.
   */
  private LocalDate positionToDate(int position) {
    try {
      DateTimeZone tz = DateTimeZone.forTimeZone(TimeZone.getDefault());
      LocalDate today = LocalDate.now(tz);
      return today.minusDays(position);
    } catch(IllegalArgumentException e) {
      Log.e("MemoryCardviewAdaptor", String.format("Error setting timezone (%s)", e.toString()));
      // Fall back to default TZ (UTC)
      return LocalDate.now().minusDays(position);
    }
  }
}
