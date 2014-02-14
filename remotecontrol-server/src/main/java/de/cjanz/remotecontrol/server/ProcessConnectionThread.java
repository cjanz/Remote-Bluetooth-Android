package de.cjanz.remotecontrol.server;

import java.awt.Robot;
import java.io.DataInputStream;

import javax.microedition.io.StreamConnection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ProcessConnectionThread implements Runnable {

	private static final Logger LOG = LogManager
			.getLogger(ProcessConnectionThread.class);

	private final StreamConnection mConnection;

	private static final int EXIT_CMD = -1;

	public ProcessConnectionThread(StreamConnection connection) {
		mConnection = connection;
	}

	@Override
	public void run() {
		try {
			// prepare to receive data
			DataInputStream inputStream = new DataInputStream(
					mConnection.openInputStream());

			LOG.info("Waiting for input...");

			while (true) {
				int command = inputStream.readInt();

				if (command == EXIT_CMD) {
					LOG.info("Finish process");
					break;
				}

				processCommand(command);
			}
		} catch (Exception e) {
			LOG.error("Error while reading input", e);
		}
	}

	/**
	 * Process the command from client
	 * 
	 * @param command
	 *            the command code
	 */
	private void processCommand(int command) {
		try {
			Robot robot = new Robot();
			LOG.debug("Pressing: " + command);
			robot.keyPress(command);
			robot.keyRelease(command);
		} catch (Exception e) {
			LOG.error("Error while executing command", e);
		}
	}
}
