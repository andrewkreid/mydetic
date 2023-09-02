package net.ghosttrails.www.mydetic;

import android.view.View;
import java.time.LocalDate;

/**
 * Interface used by MemoryCardViewAdapter to allow a listener to be assigned to taps on the
 * individual memory cards.
 */
public interface CustomItemClickListener {
  void onItemClick(View v, int position, LocalDate memoryDate);
}
