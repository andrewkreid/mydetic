package net.ghosttrails.www.mydetic;

/**
 * An interface required by Activities that contain MemoryYearFragment, MemoryMonthFragment or
 * MemoryDayFragment objects.
 */
public interface MemoryListFragmentDataProvider {

  MemoryAppInterface getAppInterface();

  int getMemoryListYear();

  int getMemoryListMonth();
}
