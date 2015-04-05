package com.alibaba.dcm;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author ding.lid
 */
public class DnsCacheManipulatorTest {
    @Test
    public void test_getAllDnsCache() throws Exception {
        DnsCacheManipulator.clearDnsCache();

        final String host = "www.test_getAllDnsCacheEntries.com";
        final String ip = "42.42.42.42";

        DnsCacheManipulator.setDnsCache(host, ip);

        final List<DnsCacheEntry> allDnsCacheEntries = DnsCacheManipulator.getAllDnsCache();
        final List<DnsCacheEntry> expected = Arrays.asList(
                new DnsCacheEntry(host.toLowerCase(), new String[]{ip}, new Date(Long.MAX_VALUE)));

        assertEquals(expected, allDnsCacheEntries);
    }

    @Test
    public void test_loadDnsCacheConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig();
        final String ip = InetAddress.getByName("www.hello1.com").getHostAddress();
        assertEquals("42.42.41.41", ip);
    }

    @Test
    public void test_loadDnsCacheConfig_fromMyConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig("my-dns-cache.properties");
        final String ip = InetAddress.getByName("www.hello1.com").getHostAddress();
        assertEquals("42.42.43.43", ip);
    }

    @Test
    public void test_configNotFound() throws Exception {
        try {
            DnsCacheManipulator.loadDnsCacheConfig("not-existed.properties");
            fail();
        } catch (DnsCacheManipulatorException expected) {
            assertEquals("Fail to find not-existed.properties on classpath!", expected.getMessage());
        }
    }

    @Test
    public void test_DnsCache_canExpire() throws Exception {
        final String notExistedHost = "www.not-existed-host-test_DnsCache_canExpire.com";

        DnsCacheManipulator.setDnsCache(30, notExistedHost, "42.42.43.43");
        final String ip = InetAddress.getByName(notExistedHost).getHostAddress();
        assertEquals("42.42.43.43", ip);

        Thread.sleep(32);

        try {
            InetAddress.getByName(notExistedHost).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected.toString());
            assertTrue(true);
        }
    }

    @Test
    public void test_removeDnsCache() throws Exception {
        final String notExistedHost = "www.not-existed-host-test_removeDnsCache";

        DnsCacheManipulator.setDnsCache(notExistedHost, "42.42.43.43");
        final String ip = InetAddress.getByName(notExistedHost).getHostAddress();
        assertEquals("42.42.43.43", ip);

        DnsCacheManipulator.removeDnsCache(notExistedHost);

        try {
            InetAddress.getByName(notExistedHost).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected.toString());
            assertTrue(true);
        }
    }

    @Test
    public void test_multi_ips_in_config_file() throws Exception {
        DnsCacheManipulator.clearDnsCache();
        DnsCacheManipulator.loadDnsCacheConfig("dns-cache-multi-ips.properties");

        final String host = "www.hello-multi-ips.com";
        DnsCacheEntry entry = new DnsCacheEntry(host,
                new String[]{"42.42.41.1", "42.42.41.2"}, new Date(Long.MAX_VALUE));
        assertEquals(entry, DnsCacheManipulator.getDnsCache(host));

        final String hostLoose = "www.hello-multi-ips-loose.com";
        DnsCacheEntry entryLoose = new DnsCacheEntry(hostLoose,
                new String[]{"42.42.41.1", "42.42.41.2", "42.42.41.3", "42.42.41.4"}, new Date(Long.MAX_VALUE));
        assertEquals(entryLoose, DnsCacheManipulator.getDnsCache(hostLoose));
    }
}