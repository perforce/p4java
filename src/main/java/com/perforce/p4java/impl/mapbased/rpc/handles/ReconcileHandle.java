package com.perforce.p4java.impl.mapbased.rpc.handles;

import com.perforce.p4java.client.ClientProgressReport;
import com.perforce.p4java.client.IProgressReport;
import com.perforce.p4java.diff.DigestTree;
import com.perforce.p4java.diff.StrStr;
import com.perforce.p4java.impl.mapbased.rpc.CommandEnv.RpcHandler;
import com.perforce.p4java.server.callback.progress.IProgress;
import com.perforce.p4java.server.callback.progress.ProgressType;
import com.perforce.p4java.server.callback.progress.ProgressUnit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ReconcileHandle extends AbstractHandle {

	private static final String RECONCILE_HANDLER_SKIP_ADD_KEY = "skipAdd";
	private static final String RECONCILE_DEL_COUNT_KEY = "delCount";
	private final Set<Integer> matchedIndex = new HashSet<>();
	private final DigestTree digestTree = new DigestTree();

	private ClientProgressReport progressReport = null;
	private String stage = "";

	public ClientProgressReport getProgressReport() {
		return progressReport;
	}

	public ReconcileHandle(RpcHandler rpcHandler) {
		super(rpcHandler);
	}

	@Override
	public String getHandleType() {
		return "ReconcileHandle";
	}

	@SuppressWarnings("unchecked")
	public List<String> getSkipFiles() {
		if (!rpcHandler.getMap().containsKey(RECONCILE_HANDLER_SKIP_ADD_KEY)) {
			List<String> list = new LinkedList<>();
			rpcHandler.getMap().put(RECONCILE_HANDLER_SKIP_ADD_KEY, list);
			return list;
		}
		return (List<String>) rpcHandler.getMap().get(RECONCILE_HANDLER_SKIP_ADD_KEY);
	}

	public void incrementDelCount() {
		long delCount = 0;
		if (rpcHandler.getMap().containsKey(RECONCILE_DEL_COUNT_KEY)) {
			delCount = (long) rpcHandler.getMap().get(RECONCILE_DEL_COUNT_KEY);
		}
		delCount++;
		rpcHandler.getMap().put(RECONCILE_DEL_COUNT_KEY, delCount);
	}

	public long getDelCount() {
		if (rpcHandler.getMap().containsKey(RECONCILE_DEL_COUNT_KEY)) {
			return (long) rpcHandler.getMap().get(RECONCILE_DEL_COUNT_KEY);
		}
		return 0;
	}

	public void setMatch(int i) {
		this.matchedIndex.add(i);
	}

	public boolean alreadyMatched(int i) {
		return this.matchedIndex.contains(i);
	}

	public String getDigest(String fileName, String digestStr) {
		digestTree.putIfAbsent(new StrStr(fileName, digestStr));
		return digestTree.get(fileName).getDigest();
	}

	public DigestTree getDigestTree() {
		return this.digestTree;
	}

	public void beginStage(String stage, IProgress progressCallback, ProgressType progressType) {
		this.stage = stage;
		if (progressCallback != null) {
			progressCallback.init(progressType);
			this.progressReport = new ClientProgressReport(progressCallback);
			this.progressReport.description(stage);
			if (ProgressType.FILES.equals(progressType)) {
				this.progressReport.units(ProgressUnit.FILES);
			}
			if (ProgressType.DIRS.equals(progressType)) {
				this.progressReport.units(ProgressUnit.DIRS);
			}
			progressReport.doReport(IProgressReport.CPP_NORMAL);
		}
	}

	public void stageComplete() {
		if (progressReport == null) {
			return;
		}
		progressReport.doReport(IProgressReport.CPP_DONE);
	}

	public void increment(long inc) {
		if (progressReport == null) {
			return;
		}
		progressReport.increment(inc);
		if (progressReport.isComplete()) {
			stageComplete();
		}
	}


}
