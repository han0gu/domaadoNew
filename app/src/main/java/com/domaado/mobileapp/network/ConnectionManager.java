package com.domaado.mobileapp.network;

import android.util.Log;

import java.util.ArrayList;

public class ConnectionManager {

	public static final int MAX_CONNECTIONS = 1;

	private ArrayList<Runnable> active = new ArrayList<Runnable>();
	private ArrayList<Runnable> queue = new ArrayList<Runnable>();

	private static ConnectionManager instance = null;

	public static ConnectionManager getInstance() {
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	public void push(Runnable runnable) {
		queue.add(runnable);
		if (active.size() < MAX_CONNECTIONS) {
			startNext();
		}
	}

	private void startNext() {
		if (!queue.isEmpty()) {
			Runnable next = queue.get(0);
			queue.remove(0);
			active.add(next);

			Thread thread = new Thread(next);
			thread.start();
		}
	}

	public void didComplete(Runnable runnable) {
		active.remove(runnable);
		startNext();
	}

	public void cancel() {
		
		while(!queue.isEmpty() && queue != null) {
			CommonHttpConnection thread = (CommonHttpConnection)queue.get(0);
			thread.cancel();
			queue.remove(thread);
			Log.i("ConnectionManager", "queue delete");
		}
		
		while(!active.isEmpty() && active != null) {
			CommonHttpConnection thread = (CommonHttpConnection)active.get(0);
			thread.cancel();
			active.remove(thread);
			Log.i("ConnectionManager", "active delete");
		}
	}

}
