package net.ghosttrails.www.mydetic;

import android.view.View;

import java.util.Date;

/**
 * Interface used by MemoryCardViewAdapter to allow a listener to be assigned to taps
 * on the individual memory cards.
 */
public interface CustomItemClickListener {
  public void onItemClick(View v, int position, Date memoryDate);
}
