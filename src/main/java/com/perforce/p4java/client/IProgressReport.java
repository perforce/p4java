package com.perforce.p4java.client;

import com.perforce.p4java.server.callback.progress.ProgressUnit;

public interface IProgressReport {
	int CPP_NORMAL = 0;
	int CPP_DONE = 1;
	int CPP_FAILDONE = 2;
	int CPP_FLUSH = 3;

	void description(String description);

	void units(ProgressUnit units);

	void total(long total);

	void position(long position);

	void position(long position, int flag);

	void increment();

	void increment(long amount);

	void increment(long amount, int flag);

	long getPosition();

	long getTotal();

	String getDescription();

	ProgressUnit getUnits();

	boolean isComplete();
}