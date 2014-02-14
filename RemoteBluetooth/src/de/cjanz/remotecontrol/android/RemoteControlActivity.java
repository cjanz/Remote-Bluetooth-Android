package de.cjanz.remotecontrol.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteControlActivity extends FragmentActivity {

	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	public static final class BluetoothMessageHandler extends Handler {

		public static final int MESSAGE_STATE_CHANGE = 1;
		public static final int MESSAGE_READ = 2;
		public static final int MESSAGE_WRITE = 3;
		public static final int MESSAGE_TOAST = 5;

		public static final String DEVICE_NAME = "device_name";
		public static final String TOAST = "toast";

		private final Context context;
		private final TextView mTitle;

		public BluetoothMessageHandler(Context context, TextView mTitle) {
			this.context = context;
			this.mTitle = mTitle;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				String deviceName = msg.getData().getString(DEVICE_NAME);
				connectionStateChanged(msg.arg1, deviceName);
				break;
			case MESSAGE_TOAST:
				Toast.makeText(context, msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		public void connectionStateChanged(int state, String deviceName) {
			switch (state) {
			case BluetoothCommandService.STATE_CONNECTED:
				mTitle.setText(R.string.title_connected_to);
				mTitle.append(deviceName);
				Toast.makeText(context, "Connected to " + deviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothCommandService.STATE_CONNECTING:
				mTitle.setText(R.string.title_connecting);
				break;
			case BluetoothCommandService.STATE_LISTEN:
			case BluetoothCommandService.STATE_NONE:
				mTitle.setText(R.string.title_not_connected);
				break;
			}
		}
	}

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothMessageHandler mHandler;
	private ServiceConnectorFragment connectorFragment;
	private TextView mStatusText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		mStatusText = (TextView) findViewById(R.id.title_left_text);
		mStatusText.setText(R.string.app_name);
		mStatusText = (TextView) findViewById(R.id.title_right_text);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		connectorFragment = setupServiceConnector();
		mHandler = new BluetoothMessageHandler(getApplicationContext(),
				mStatusText);
	}

	private ServiceConnectorFragment setupServiceConnector() {
		FragmentManager fm = getSupportFragmentManager();
		ServiceConnectorFragment fragment = (ServiceConnectorFragment) fm
				.findFragmentByTag("task");

		// If the Fragment is non-null, then it is currently being
		// retained across a configuration change.
		if (fragment == null) {
			fragment = new ServiceConnectorFragment();
			fm.beginTransaction().add(fragment, "task").commit();
		}

		return fragment;
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}

		mHandler.connectionStateChanged(connectorFragment.getService()
				.getState(), connectorFragment.getService().getDeviceName());
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				connectorFragment.getService().connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode != Activity.RESULT_OK) {
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		}
		return false;
	}

	public void onKey(View view) {
		connectorFragment.getService().write(
				Integer.valueOf((String) view.getTag()));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			connectorFragment.getService().write(39);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			connectorFragment.getService().write(37);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public BluetoothMessageHandler getHandler() {
		return mHandler;
	}

}