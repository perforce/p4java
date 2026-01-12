package com.perforce.p4java.client;

import com.perforce.p4java.server.callback.progress.ProgressUnit;

/**
 * Base implementation of IProgressReport with state tracking.
 * Uses boolean flags instead of bitwise operations.
 * Corresponds to ProgressReport class in C++.
 */
public abstract class BaseProgressReport implements IProgressReport {

	// ========== Boolean Change Flags ==========
	protected boolean descriptionChanged;
	protected boolean unitsChanged;
	protected boolean totalChanged;
	protected boolean positionChanged;
	protected boolean isNewProgress;


	// ========== Instance Variables ==========
	protected String description;
	protected ProgressUnit units;
	protected long total;

	protected long position;
	protected boolean needFinal;
	protected long lastUpdateTime;


	public BaseProgressReport() {
		this.descriptionChanged = false;
		this.unitsChanged = false;
		this.totalChanged = false;
		this.positionChanged = false;
		this.isNewProgress = true;
		this.description = "";
		this.units = ProgressUnit.UNSPECIFIED;
		this.total = 0;
		this.position = 0;
		this.needFinal = false;
		this.lastUpdateTime = System.currentTimeMillis();
	}


	// ========== Interface Implementation ==========

	@Override
	public void description(String description) {
		this.description = description;
		this.descriptionChanged = true;
	}

	@Override
	public void units(ProgressUnit units) {
		this.units = units;
		this.unitsChanged = true;
	}

	@Override
	public void total(long total) {
		this.total = total;
		this.totalChanged = true;
	}

	@Override
	public void position(long position) {
		position(position, CPP_NORMAL);
	}

	@Override
	public void position(long position, int flag) {
		if (this.position != position) {
			this.position = position;
			this.positionChanged = true;
		}
		considerReport(flag);
	}

	@Override
	public void increment() {
		increment(1, CPP_NORMAL);
	}

	@Override
	public void increment(long amount) {
		increment(amount, CPP_NORMAL);
	}

	@Override
	public void increment(long amount, int flag) {
		if (amount != 0) {
			this.position += amount;
			this.positionChanged = true;
		}
		considerReport(flag);
	}

	@Override
	public long getPosition() {
		return position;
	}

	@Override
	public long getTotal() {
		return total;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ProgressUnit getUnits() {
		return units;
	}


	protected void considerReport(int flag) {

	/*	if (flag == CPP_NORMAL) {
			long now = System.currentTimeMillis();
			long elapsed = now - lastUpdateTime;
			if (elapsed < 1) {
				lastUpdateTime = System.currentTimeMillis();
				return;
			}
		}*/
		doReport(flag);
	}

	protected void clearChangeFlags() {
		descriptionChanged = false;
		unitsChanged = false;
		totalChanged = false;
		positionChanged = false;
		isNewProgress = false;
	}

	protected abstract void doReport(int flag);


	@Override
	protected void finalize() throws Throwable {
		try {
			if (needFinal) {
				doReport(CPP_FAILDONE);
			}
		} finally {
			super.finalize();
		}
	}
}