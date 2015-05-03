package net.ghosttrails.www.mydetic.api;

import junit.framework.TestCase;

import java.util.Date;

/**
 * Unit Tests for InRamMemoryApi
 */
public class InRamMemoryApiTest extends TestCase {

    private InRamMemoryApi api;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.api = new InRamMemoryApi();
    }

    public void testAddAndRetrieve() {
        String userId = "theUserID";
        Date date = new Date(2014,5,3);

        assertEquals("Should have no memories", 0, api.getMemories(userId).getDates().size());

        api.putMemory(userId, new MemoryData(userId, "a memory", date));
        assertTrue(api.hasMemory(userId, date));

        MemoryDataList list = api.getMemories(userId);
        assertEquals("Should have one memory", 1, list.getDates().size());
        assertTrue(list.hasDate(date));

        try {
            MemoryData md = api.getMemory(userId, date);
            assertEquals(userId, md.getUserId());
            assertEquals(date, md.getMemoryDate());
            assertEquals("a memory", md.getMemoryText());
        } catch(NoMemoryFoundException nme) {
            fail(nme.getMessage());
        }

        try {
            MemoryData md = api.getMemory("different user", date);
            fail("Should have thrown NoMemoryFoundException");
        } catch(NoMemoryFoundException nme) {
            // expected.
        }
    }

    public void testUpdate() {

    }

    public void testRetrieveRange() {

    }

    public void testDelete() {

    }
}
