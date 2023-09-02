package net.ghosttrails.www.mydetic.api;

import java.util.ArrayList;
import java.util.List;
import net.ghosttrails.www.mydetic.exceptions.MyDeticReadFailedException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticWriteFailedException;
import java.time.LocalDate;

/** Utility class to populate some memories */
public class SampleSetPopulator {

  private static String loremIpsumStr =
      "Lorem ipsum dolor sit amet, "
          + "consectetur adipiscing elit, sed do eiusmod tempor incididunt ut "
          + "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud "
          + "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. "
          + "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
          + "dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non"
          + " proident, sunt in culpa qui officia deserunt mollit anim id est "
          + "laborum.";

  /** Create a sample set of memories for testing. */
  public static void populateTestSet(InRamMemoryApi api, String userId, boolean removeExisting)
      throws MyDeticReadFailedException, MyDeticWriteFailedException, CloneNotSupportedException {
    if (removeExisting) {
      api.clearMemories(userId);
    }

    ArrayList<MemoryData> memories = new ArrayList<>();

    addMemory(memories, userId, 2014, 1, 1, "New Years Day 2014");
    addMemory(memories, userId, 2015, 4, 28, "Today was a good day");
    addMemory(memories, userId, 2015, 4, 29, "Dreamt about cheese.");
    addMemory(memories, userId, 2015, 4, 30, "Accidentally shaved off my hipster beard");
    addMemory(memories, userId, 2015, 5, 3, "I don't like cabbage with vegemite.");
    addMemory(memories, userId, 2015, 5, 4, "Winter is coming.");
    addMemory(
        memories,
        userId,
        2015,
        5,
        5,
        "It is possible I already had some presentiment of my future. The "
            + "locked and rusted gate that stood before us, with wisps of "
            + "river fog threading its spikes like the mountain paths, remains "
            + "in my mind now as the symbol of my exile. That is why I have "
            + "begun this"
            + " account of it with the aftermath of our swim, in which I, the "
            + "torturer’s apprentice Severian, had so nearly drowned.");

    // Some text that's long enough to need to be scrollable.
    addMemory(
        memories,
        userId,
        2015,
        5,
        6,
        "Four score and seven years ago our fathers brought forth on this "
            + "continent a new nation, conceived in liberty, and dedicated to "
            + "the proposition that all men are created equal.\n\nNow we are "
            + "engaged in a great civil war, testing whether that nation, or "
            + "any nation so conceived and so dedicated, can long endure. We "
            + "are met on a great battlefield of that war. We have come to "
            + "dedicate a portion of that field, as a final resting place for "
            + "those who here gave their lives that that nation might live. It "
            + "is altogether fitting and proper that we should do this.\n\nBut,"
            + " in a larger sense, we can not dedicate, we can not consecrate, "
            + "we can not hallow this ground. The brave men, living and dead, "
            + "who struggled here, have consecrated it, far above our poor "
            + "power to "
            + "add or detract. The world will little note, nor long remember "
            + "what we say here, but it can never forget what they did here. It"
            + " is for "
            + "us the living, rather, to be dedicated here to the unfinished "
            + "work which they who fought here have thus far so nobly advanced."
            + " It is "
            + "rather for us to be here dedicated to the great task remaining "
            + "before us—that from these honored dead we take increased "
            + "devotion to that cause for which they gave the last full measure"
            + " of devotion—"
            + "that we here highly resolve that these dead shall not have died "
            + "in vain—that this nation, under God, shall have a new birth of "
            + "freedom—and that government of the people, by the people, for "
            + "the people, shall not perish from the earth.");

    for (int i = 7; i < 20; i++) {
      addMemory(memories, userId, 2015, 5, i, loremIpsumStr);
    }

    api.populateMemories(userId, memories);
  }

  public static void addMemory(
      List<MemoryData> list, String userId, int year, int month, int day, String memoryText) {
    LocalDate localDate = LocalDate.of(year, month, day);
    list.add(new MemoryData(userId, memoryText, localDate));
  }
}
