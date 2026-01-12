package com.perforce.p4java.client;

import com.perforce.p4java.server.callback.progress.IProgress;


public class ClientProgressReport extends BaseProgressReport {


	protected IProgress cp;

	public ClientProgressReport(IProgress cp) {
		super();
		if (cp == null) {
			throw new IllegalArgumentException("ClientProgress cannot be null");
		}
		this.cp = cp;
	}


	@Override
	public void doReport(int flag) {
		if (cp == null) {
			return;
		}

		if (descriptionChanged || unitsChanged) {
			cp.description(description, units);
		}


		if (totalChanged) {
			cp.total(total);
		}


		if (positionChanged) {
			cp.update(position);
		}


		clearChangeFlags();


		if (flag == CPP_DONE || flag == CPP_FAILDONE) {
			if (flag == CPP_FAILDONE)
				cp.done(1);
			else
				cp.done(0);
			needFinal = false;
		}
	}


	public IProgress getClientProgress() {
		return cp;
	}

	public void close() {
		if (needFinal) {
			doReport(CPP_FAILDONE);
		}
	}

	@Override
	public boolean isComplete() {
		return total > 0 && position == total;
	}

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