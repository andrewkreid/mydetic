package net.ghosttrails.www.mydetic.api;

import junit.framework.TestCase;

import net.ghosttrails.www.mydetic.exceptions.MyDeticNoMemoryFoundException;

import java.util.Date;

/**
 * Unit Tests for InRamMemoryApi
 */
public class InRamMemoryApiTest extends TestCase {

    private InRamMemoryApi api;
    String userId = "theUserID";
    Date date = new Date(2014,5,3);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.api = new InRamMemoryApi();
    }

    public void testAddAndRetrieve() {

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
        } catch(MyDeticNoMemoryFoundException nme) {
            fail(nme.getMessage());
        }

        try {
            MemoryData md = api.getMemory("different user", date);
            fail("Should have thrown MyDeticNoMemoryFoundException");
        } catch(MyDeticNoMemoryFoundException nme) {
            // expected.
        }
    }

    public void testUpdate() throws MyDeticNoMemoryFoundException {
        api.putMemory(userId, new MemoryData(userId, "a memory", date));
        MemoryData md = api.getMemory(userId, date);
        assertEquals("a memory", md.getMemoryText());

        api.putMemory(userId, new MemoryData(userId, "another memory", date));
        md = api.getMemory(userId, date);
        assertEquals("another memory", md.getMemoryText());
    }

    public void testRetrieveRange() {

        api.putMemory(userId, new MemoryData(userId, "2014-05-01", new Date(2014, 5, 1)));
        api.putMemory(userId, new MemoryData(userId, "2014-05-02", new Date(2014, 5, 2)));
        api.putMemory(userId, new MemoryData(userId, "2014-05-03", new Date(2014, 5, 3)));
        api.putMemory(userId, new MemoryData(userId, "2014-05-04", new Date(2014, 5, 4)));
        api.putMemory(userId, new MemoryData(userId, "2014-05-05", new Date(2014, 5, 5)));

        assertEquals(5, api.getMemories(userId).getDates().size());
        assertEquals(5, api.getMemories(userId, null, null).getDates().size());
        assertEquals(4, api.getMemories(userId, new Date(2014, 5, 2), null).getDates().size());
        assertEquals(5, api.getMemories(userId, new Date(2014, 5, 1), null).getDates().size());
        assertEquals(5, api.getMemories(userId, null, new Date(2014, 5, 5)).getDates().size());
        assertEquals(4, api.getMemories(userId, null, new Date(2014, 5, 4)).getDates().size());
        assertEquals(3, api.getMemories(userId, new Date(2014, 5, 2), new Date(2014, 5, 4)).getDates().size());
        assertEquals(1, api.getMemories(userId, new Date(2014, 5, 3), new Date(2014, 5, 3)).getDates().size());
    }

    public void testDelete() throws MyDeticNoMemoryFoundException {
        api.putMemory(userId, new MemoryData(userId, "2014-05-01", new Date(2014, 5, 1)));
        api.putMemory(userId, new MemoryData(userId, "2014-05-02", new Date(2014, 5, 2)));
        api.putMemory(userId, new MemoryData(userId, "2014-05-03", new Date(2014, 5, 3)));

        assertEquals(3, api.getMemories(userId).getDates().size());
        api.deleteMemory(userId, new Date(2014, 5, 1));
        assertEquals(2, api.getMemories(userId).getDates().size());
        assertFalse(api.hasMemory(userId, new Date(2014, 5, 1)));

        try {
            api.deleteMemory(userId, new Date(2014, 5, 1));
            fail("Should have thrown exception");
        } catch(MyDeticNoMemoryFoundException nme) {
            // Should have thrown exception
        }
    }
}
