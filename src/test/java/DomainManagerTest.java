import org.testng.annotations.Test;

import sftp.DomainManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import static org.testng.Assert.*;

public class DomainManagerTest {

    @Test
    public void parseAndSaveRoundtrip() throws Exception {
        String json = "{ \"addresses\": [" +
                "{\"domain\":\"a.com\",\"ip\":\"1.1.1.1\"}," +
                "{\"domain\":\"b.com\",\"ip\":\"2.2.2.2\"}" +
                "]}";

        DomainManager dm = new DomainManager();
        try (InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"))) {
            dm.loadFromInputStream(is);
        }
        Map<String,String> all = dm.getAllSorted();
        assertEquals(all.get("a.com"), "1.1.1.1");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dm.saveToStream(baos);
        String back = baos.toString("UTF-8");
        assertTrue(back.contains("\"a.com\""));
        assertTrue(back.contains("\"1.1.1.1\""));
    }

    @Test
    public void addUniquePair_ok() {
        DomainManager dm = new DomainManager();
        boolean ok = dm.addPair("new.domain", "10.10.10.10");
        assertTrue(ok);
    }

    @Test
    public void addDuplicateDomainFails() {
        DomainManager dm = new DomainManager();
        assertTrue(dm.addPair("dup.com", "1.1.1.1"));
        assertFalse(dm.addPair("dup.com", "2.2.2.2"));
    }

    @Test
    public void addInvalidIpFails() {
        DomainManager dm = new DomainManager();
        assertFalse(dm.addPair("bad.com", "01.1.1.1")); // лидирующие нули
        assertFalse(dm.addPair("bad.com", "256.1.1.1"));
    }

}
