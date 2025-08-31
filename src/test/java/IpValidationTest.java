import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import sftp.IpValidator;

import static org.testng.Assert.*;

public class IpValidationTest {

    static boolean isValidIPv4(String ip) {
        return ip != null && ip.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    }

    @DataProvider
    public Object[][] validIps() {
        return new Object[][]{
                {"0.0.0.0"}, {"1.1.1.1"}, {"127.0.0.1"},
                {"10.0.0.1"}, {"192.168.255.255"}, {"255.255.255.255"}
        };
    }

    @DataProvider
    public Object[][] invalidIps() {
        return new Object[][]{
                {null}, {""}, {"1.1.1"}, {"1.1.1.1.1"},
                {"256.1.1.1"}, {"1.256.1.1"}, {"1.1.256.1"}, {"1.1.1.256"},
                {"01.1.1.1"}, {"a.b.c.d"}, {"..."}, {" 1.1.1.1"}, {"1.1.1.1 "}
        };
    }

    @Test(dataProvider = "validIps")
    public void valid(String ip) { assertTrue(IpValidator.isValidIPv4(ip)); }

    @Test(dataProvider = "invalidIps")
    public void invalid(String ip) { assertFalse(IpValidator.isValidIPv4(ip)); }
}
