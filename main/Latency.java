package main;

import java.util.Vector;

public class Latency {
	private static final int LATENCY_AVG_BUFFER_SIZE = 50;
	private Vector<Integer> _latencyBuffer;
	private long _latestSent;
	
	public Latency() {
		this._latencyBuffer = new Vector<Integer>();
		this._latestSent = 0;
	}
	
	public void setLatestSent() {
		this._latestSent = System.currentTimeMillis();
	}
	
	public int latencySamplesCount() {
		return this._latencyBuffer.size();
	}
	
	public int latencySamplesMax() {
		return LATENCY_AVG_BUFFER_SIZE;
	}
	
	public int latencyAvg() {
		int size = _latencyBuffer.size();
		if(size == 0)
			return 0;
		int latency = 0;
		for(int i : _latencyBuffer)
			latency += i;
		return latency / size;
	}
	
	public void updateLatency() {
		if(this._latestSent == 0)
			return;
		long timer = System.currentTimeMillis();
		this._latencyBuffer.add((int) (timer - this._latestSent));
		this._latestSent = 0;
		if(this._latencyBuffer.size() > LATENCY_AVG_BUFFER_SIZE)
			this._latencyBuffer.remove(0);
	}
}
