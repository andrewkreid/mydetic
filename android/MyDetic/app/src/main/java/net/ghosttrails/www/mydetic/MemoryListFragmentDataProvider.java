package net.ghosttrails.www.mydetic;

import net.ghosttrails.www.mydetic.api.MemoryApi;

/**
 * An interface required by Activities that contain MemoryYearFragment, MemoryMonthFragment or
 * MemoryDayFragment objects.
 */
public interface MemoryListFragmentDataProvider {

  public MemoryAppInterface getAppInterface();
  public int getMemoryListYear();
  public int getMemoryListMonth();

}
