package pl.m4.wirelesswheel;

import junit.framework.TestCase;

import org.junit.Test;


public class TcpClientTest extends TestCase{

    @Test
    public void testCheckIpFromAddress() {
        TcpClient tcp = new TcpClient();
        String ip = tcp.getHost("192.168.1.1:8080");
        assertEquals("192.168.1.1", ip);
    }

    @Test
    public void testCheckPortFromAddress() {
        TcpClient tcp = new TcpClient();
        int port = tcp.getPort("192.168.1.1:8080");
        assertEquals(8080, port);
    }

}
