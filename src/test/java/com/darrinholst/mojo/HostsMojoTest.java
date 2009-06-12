package com.darrinholst.mojo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class HostsMojoTest {
	@Test
	public void testExecute() throws Exception {
		File original = createFile("original file");

		Map<String, String> hosts = new HashMap<String, String>();
		hosts.put("host", "ip");

		ShuntedHostsMojo mojo = new ShuntedHostsMojo();
		mojo.hostsFile = original.getAbsolutePath();
		mojo.hosts = hosts;

		mojo.execute();

		assertEquals("original file\nip host\n", FileUtils.readFileToString(original));
		assertEquals("original file", FileUtils.readFileToString(new File(original.getAbsoluteFile() + HostsMojo.BACKUP_SUFFIX)));
		assertTrue(mojo.addShutdownHook_called);
	}

	@Test
	public void testExecuteTwice() throws Exception {
		File original = createFile("original file");

		Map<String, String> hosts = new HashMap<String, String>();
		hosts.put("host", "ip");

		ShuntedHostsMojo mojo = new ShuntedHostsMojo();
		mojo.hostsFile = original.getAbsolutePath();
		mojo.hosts = hosts;

		mojo.execute();

		hosts = new HashMap<String, String>();
		hosts.put("host2", "ip2");
		mojo.hosts = hosts;

		mojo.execute();

		assertEquals("original file" + "\n" + "ip host" + "\n\n" + "ip2 host2" + "\n", FileUtils.readFileToString(original));
		assertEquals("original file", FileUtils.readFileToString(new File(original.getAbsoluteFile() + HostsMojo.BACKUP_SUFFIX)));
		assertTrue(mojo.addShutdownHook_called);
	}

	@Test
	public void testShutdownThreadCleansUp() throws Exception {
		File original = createFile("modified");
		File backup = new File(original.getAbsolutePath() + HostsMojo.BACKUP_SUFFIX);
		backup.createNewFile();
		FileUtils.writeStringToFile(backup, "original");

		HostsMojo mojo = new HostsMojo();
		mojo.hostsFile = original.getAbsolutePath();
		mojo.new ShutdownThread().run();

		assertFalse(backup.exists());
		assertEquals("original", FileUtils.readFileToString(original));
	}

	@Test
	public void testShutdownThreadWhenNoBackup() throws Exception {
		File original = createFile("original");

		HostsMojo mojo = new HostsMojo();
		mojo.hostsFile = original.getAbsolutePath();
		mojo.new ShutdownThread().run();

		assertEquals("original", FileUtils.readFileToString(original));
	}

	private File createFile(String content) throws IOException {
		File original = File.createTempFile("prefix", "suffix");
		FileUtils.writeStringToFile(original, content);
		return original;
	}

	private class ShuntedHostsMojo extends HostsMojo {
		public boolean addShutdownHook_called = false;

		@Override
		protected void addShutdownHook() {
			this.addShutdownHook_called = true;
		}
	}
}
