package org.glandais.garmin.wms2jnx;

import java.util.LinkedList;

public class WorkQueue {
	private final PoolWorker[] threads;
	private final LinkedList<Runnable> queue;
	private boolean done = false;

	public WorkQueue(int nThreads) {
		queue = new LinkedList<Runnable>();
		threads = new PoolWorker[nThreads];

		for (int i = 0; i < nThreads; i++) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	public int queueSize() {
		synchronized (queue) {
			return queue.size();
		}
	}

	public void execute(Runnable r) {
		synchronized (queue) {
			queue.addLast(r);
			queue.notify();
		}
	}

	private class PoolWorker extends Thread {
		public void run() {
			Runnable r;

			while (!isDone()) {
				synchronized (queue) {
					while (queue.isEmpty() && !isDone()) {
						try {
							queue.wait();
						} catch (InterruptedException ignored) {
						}
					}

					r = (Runnable) queue.removeFirst();
				}

				// If we don't catch RuntimeException,
				// the pool could leak threads
				try {
					r.run();
				} catch (RuntimeException e) {
					// You might want to log something here
				}
			}
		}

	}

	private boolean isDone() {
		synchronized (this) {
			return done;
		}
	}

	public void done() {
		synchronized (this) {
			done = true;
			queue.notify();
		}
	}

}
