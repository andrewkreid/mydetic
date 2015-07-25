package net.ghosttrails.www.mydetic;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;

/**
 * Adaptor to display memory cards for the last NUM_CARDS days.
 */
public class MemoryCardviewAdaptor extends
    RecyclerView.Adapter<MemoryCardviewAdaptor.ViewHolder> {

  // How many days into the past to show days for.
  static final int NUM_CARDS = 7;

  private MemoryAppInterface app;

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class ViewHolder extends RecyclerView.ViewHolder {
    public CardView mView;
    public TextView titleView;

    public ViewHolder(CardView v) {
      super(v);
      mView = v;
      titleView = (TextView) mView.findViewById(R.id.memory_card_view_title);
    }

    public void fillCard(Date d) {
      titleView.setText(Utils.isoFormat(d));
    }

    public void fillCard(MemoryData memory) {
      titleView.setText(Utils.isoFormat(memory.getMemoryDate()));
      // TODO: Fill in rest of card.
    }
  }

  // Provide a suitable constructor (depends on the kind of dataset)
  public MemoryCardviewAdaptor(MemoryAppInterface app) {
    this.app = app;
  }

  // Create new views (invoked by the layout manager)
  @Override
  public MemoryCardviewAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // create a new view
    CardView v = (CardView) LayoutInflater.from(parent.getContext())
        .inflate(R.layout.memory_card, parent, false);
    // TODO set the view's size, margins, paddings and layout parameters
    return new ViewHolder(v);
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    Date memoryDate = positionToDate(position);
    MemoryData memory = app.getCachedMemory(memoryDate);
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
  private Date positionToDate(int position) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -position);
    return cal.getTime();
  }
}
