import org.testng.annotations.*;
import sftp.SftpManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.testng.Assert.*;

public class SftpIntegrationTest {

    private SftpManager mng;

    @BeforeClass
    @Parameters({"host","port","user","password"})
    public void connect(String host, @Optional("22") int port, String user, String password) {
        mng = new SftpManager();
        boolean ok = mng.connect(host, port, user, password);
        assertTrue(ok, "SFTP connect should succeed");
    }

    @AfterClass
    public void tearDown() {
        if (mng != null) mng.disconnect();
    }

    @Test
    @Parameters({"remoteFile"})
    public void getAndPut(String remoteFile) throws Exception {
        // GET
        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        try (InputStream is = mng.getChannel().get(remoteFile)) {
            byte[] buf = new byte[4096]; int n;
            while((n = is.read(buf)) != -1) copy.write(buf, 0, n);
        }
        String original = new String(copy.toByteArray(), "UTF-8");
        assertTrue(original.contains("addresses"));

        // PUT (минимальное изменение и возврат)
        String modified = original.replace("addresses", "addresses");
        try (InputStream in = new ByteArrayInputStream(modified.getBytes("UTF-8"))) {
            mng.uploadFile(in, remoteFile);
        }
        // GET снова
        ByteArrayOutputStream round = new ByteArrayOutputStream();
        try (InputStream is = mng.getChannel().get(remoteFile)) {
            byte[] buf = new byte[4096]; int n;
            while((n = is.read(buf)) != -1) round.write(buf, 0, n);
        }
        assertEquals(round.toString("UTF-8"), modified);
    }
}
