package de.cjanz.remotecontrol.server;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.InputStream;

import javax.microedition.io.StreamConnection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ProcessConnectionThread implements Runnable {

	private static final Logger LOG = LogManager
			.getLogger(ProcessConnectionThread.class);

	private final StreamConnection mConnection;

	private static final int EXIT_CMD = -1;
	private static final int KEY_RIGHT = 1;
	private static final int KEY_LEFT = 2;

	public ProcessConnectionThread(StreamConnection connection) {
		mConnection = connection;
	}

	@Override
	public void run() {
		try {
			// prepare to receive data
			InputStream inputStream = mConnection.openInputStream();

			LOG.info("Waiting for input...");

			while (true) {
				int command = inputStream.read();

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
			switch (command) {
			case KEY_RIGHT:
				LOG.debug("Right");
				robot.keyPress(KeyEvent.VK_RIGHT);
				robot.keyRelease(KeyEvent.VK_RIGHT);
				break;
			case KEY_LEFT:
				LOG.debug("Left");
				robot.keyPress(KeyEvent.VK_LEFT);
				robot.keyRelease(KeyEvent.VK_LEFT);
				break;
			}
		} catch (Exception e) {
			LOG.error("Error while executing command", e);
		}
	}
}
