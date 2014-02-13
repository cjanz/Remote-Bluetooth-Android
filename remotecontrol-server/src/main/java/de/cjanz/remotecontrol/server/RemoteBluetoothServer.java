package de.cjanz.remotecontrol.server;

import java.io.IOException;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RemoteBluetoothServer {

	private static final Logger LOG = LogManager.getLogger(RemoteBluetoothServer.class);
	
	private static final String SPP_UUID = "0000110100001000800000805F9B34FB";

	public static void main(String[] args) throws IOException {
		waitForConnection();
	}

	private static void waitForConnection() throws IOException {
		StreamConnectionNotifier notifier = acquireConnectionNotifier();

		while (true) {
			try {
				LOG.info("Waiting for connection...");
				StreamConnection connection = notifier.acceptAndOpen();
				Thread processThread = new Thread(new ProcessConnectionThread(
						connection));
				processThread.start();
			} catch (Exception e) {
				LOG.error("Error while accepting connection", e);
			}
		}
	}

	private static StreamConnectionNotifier acquireConnectionNotifier()
			throws IOException {

		UUID uuid = new UUID(SPP_UUID, false);
		String url = "btspp://localhost:" + uuid.toString()
				+ ";name=RemoteBluetooth;authenticate=false;encrypt=false;";
		
		LOG.info("Starting server: " + url);
		
		return (StreamConnectionNotifier) Connector.open(url);
	}
}
