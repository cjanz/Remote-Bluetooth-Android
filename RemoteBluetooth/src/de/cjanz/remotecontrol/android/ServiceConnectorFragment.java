package de.cjanz.remotecontrol.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ServiceConnectorFragment extends Fragment {

	private RemoteControlActivity remoteControlActivity;

	private BluetoothCommandService mCommandService = new BluetoothCommandService();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.remoteControlActivity = (RemoteControlActivity) activity;
	}

	@Override
	public void onStart() {
		super.onStart();

		mCommandService.setHandler(remoteControlActivity.getHandler());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mCommandService.stop();
	}

	@Override
	public void onDetach() {
		super.onDetach();

		this.remoteControlActivity = null;
	}

	public BluetoothCommandService getService() {
		return this.mCommandService;
	}

}
