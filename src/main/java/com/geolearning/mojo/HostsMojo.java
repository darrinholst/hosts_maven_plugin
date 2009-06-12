package com.geolearning.mojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal switch
 */
public class HostsMojo extends AbstractMojo {
	private static final String[] DEFAULT_HOSTS_FILES = new String[] { "/etc/hosts", "C:\\WINDOWS\\system32\\drivers\\etc\\hosts" };
	protected static final String BACKUP_SUFFIX = ".hosts-maven-plugin";

	/**
	 * @parameter
	 * @required
	 */
	protected Map<String, String> hosts;

	/**
	 * @parameter
	 */
	protected String hostsFile;

	public void execute() throws MojoExecutionException, MojoFailureException {
		File file = getHostsFile();
		addShutdownHook();
		backup(file);
		writeNewFile(file);
	}

	protected File getHostsFile() throws MojoExecutionException {
		String[] filesToCheck = DEFAULT_HOSTS_FILES;

		if (hostsFile != null) {
			filesToCheck = new String[] { hostsFile };
		}

		for (String hostFile : filesToCheck) {
			File file = new File(hostFile);

			if (file.exists() && file.canWrite()) {
				return file;
			}
		}

		throw new MojoExecutionException("unable to find hosts file or hosts file is not writable");
	}

	protected void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new ShutdownThread());
	}

	protected class ShutdownThread extends Thread {
		@Override
		public void run() {
			try {
				File file = getHostsFile();
				File backupFile = new File(file.getAbsolutePath() + BACKUP_SUFFIX);

				if (backupFile.exists()) {
					FileUtils.writeStringToFile(file, FileUtils.readFileToString(backupFile));
					backupFile.delete();
				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void backup(File file) throws MojoExecutionException {
		try {
			File backupFile = new File(file.getAbsolutePath() + BACKUP_SUFFIX);
			backupFile.createNewFile();
			FileUtils.copyFile(file, backupFile);
		}
		catch (Exception e) {
			throw new MojoExecutionException("unable to create backup file", e);
		}
	}

	protected void writeNewFile(File file) {
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(new FileWriter(file, true));

			for (Map.Entry<String, String> hostEntry : hosts.entrySet()) {
				writer.println();
				writer.println(hostEntry.getValue() + " " + hostEntry.getKey());
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
