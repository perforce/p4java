/*
 * Copyright 2008 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.impl.mapbased.server;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.Log;
import com.perforce.p4java.Metadata;
import com.perforce.p4java.admin.IDbSchema;
import com.perforce.p4java.admin.IDiskSpace;
import com.perforce.p4java.admin.ILogTail;
import com.perforce.p4java.admin.IProperty;
import com.perforce.p4java.admin.IProtectionEntry;
import com.perforce.p4java.admin.ITriggerEntry;
import com.perforce.p4java.admin.ServerConfigurationValue;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.IBranchSpecSummary;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.IExtension;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.core.IFix;
import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelSummary;
import com.perforce.p4java.core.ILicense;
import com.perforce.p4java.core.ILicenseLimits;
import com.perforce.p4java.core.IRepo;
import com.perforce.p4java.core.IReviewChangelist;
import com.perforce.p4java.core.IServerIPMACAddress;
import com.perforce.p4java.core.IServerProcess;
import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamIntegrationStatus;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamlog;
import com.perforce.p4java.core.IUser;
import com.perforce.p4java.core.IUserGroup;
import com.perforce.p4java.core.IUserSummary;
import com.perforce.p4java.core.file.DiffType;
import com.perforce.p4java.core.file.FileStatAncilliaryOptions;
import com.perforce.p4java.core.file.FileStatOutputOptions;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileAnnotation;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSize;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.core.file.IObliterateResult;
import com.perforce.p4java.env.SystemInfo;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.graph.ICommit;
import com.perforce.p4java.graph.IGraphListTree;
import com.perforce.p4java.graph.IGraphObject;
import com.perforce.p4java.graph.IGraphRef;
import com.perforce.p4java.graph.IRevListCommit;
import com.perforce.p4java.impl.generic.core.Extension;
import com.perforce.p4java.impl.generic.core.ExtensionSummary;
import com.perforce.p4java.impl.generic.core.ListData;
import com.perforce.p4java.impl.mapbased.rpc.RpcPropertyDefs;
import com.perforce.p4java.impl.mapbased.server.cmd.AttributeDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.BranchDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.BranchesDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ChangeDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ChangesDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ClientDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ClientsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.CommitDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ConfigureDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.CounterDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.CountersDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.DBSchemaDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.DepotDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.DepotsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.DescribeDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.Diff2Delegator;
import com.perforce.p4java.impl.mapbased.server.cmd.DirsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.DiskspaceDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.DuplicateDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ExportDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ExtensionDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.FileAnnotateDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.FileLogDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.FilesDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.FixDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.FixesDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.FstatDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.GraphListTreeDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.GraphReceivePackDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.GraphRevListDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.GraphShowRefDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.GrepDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.GroupDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.GroupsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.IListDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.InfoDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.IntegratedDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.InterchangesDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.JobDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.JobSpecDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.JobsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.JournalWaitDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.KeyDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.KeysDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.LabelDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.LabelsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.LicenseDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ListDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.LogTailDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.Login2Delegator;
import com.perforce.p4java.impl.mapbased.server.cmd.LoginDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.LogoutDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.MonitorDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.MoveDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ObliterateDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.OpenedDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.PasswdDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.PrintDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.PropertyDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ProtectDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ProtectsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ReloadDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.RenameUserDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.RenameClientDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ReposDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ResultListBuilder;
import com.perforce.p4java.impl.mapbased.server.cmd.ReviewDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.ReviewsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.SearchDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.SizesDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.SpecDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.StatDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.StreamDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.StreamlogDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.StreamsDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.TagDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.TriggersDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.UnloadDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.UserDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.UsersDelegator;
import com.perforce.p4java.impl.mapbased.server.cmd.VerifyDelegator;
import com.perforce.p4java.option.UsageOptions;
import com.perforce.p4java.option.server.ChangelistOptions;
import com.perforce.p4java.option.server.ConvertSparseOptions;
import com.perforce.p4java.option.server.CounterOptions;
import com.perforce.p4java.option.server.DeleteBranchSpecOptions;
import com.perforce.p4java.option.server.DeleteClientOptions;
import com.perforce.p4java.option.server.DeleteLabelOptions;
import com.perforce.p4java.option.server.DescribeOptions;
import com.perforce.p4java.option.server.DuplicateRevisionsOptions;
import com.perforce.p4java.option.server.ExportRecordsOptions;
import com.perforce.p4java.option.server.FixJobsOptions;
import com.perforce.p4java.option.server.GetBranchSpecOptions;
import com.perforce.p4java.option.server.GetBranchSpecsOptions;
import com.perforce.p4java.option.server.GetChangelistDiffsOptions;
import com.perforce.p4java.option.server.GetChangelistsOptions;
import com.perforce.p4java.option.server.GetClientTemplateOptions;
import com.perforce.p4java.option.server.GetClientsOptions;
import com.perforce.p4java.option.server.GetCountersOptions;
import com.perforce.p4java.option.server.GetDepotFilesOptions;
import com.perforce.p4java.option.server.GetDepotsOptions;
import com.perforce.p4java.option.server.GetDirectoriesOptions;
import com.perforce.p4java.option.server.GetExtendedFilesOptions;
import com.perforce.p4java.option.server.GetFileAnnotationsOptions;
import com.perforce.p4java.option.server.GetFileContentsOptions;
import com.perforce.p4java.option.server.GetFileDiffsOptions;
import com.perforce.p4java.option.server.GetFileSizesOptions;
import com.perforce.p4java.option.server.GetFixesOptions;
import com.perforce.p4java.option.server.GetInterchangesOptions;
import com.perforce.p4java.option.server.GetJobsOptions;
import com.perforce.p4java.option.server.GetKeysOptions;
import com.perforce.p4java.option.server.GetLabelsOptions;
import com.perforce.p4java.option.server.GetPropertyOptions;
import com.perforce.p4java.option.server.GetProtectionEntriesOptions;
import com.perforce.p4java.option.server.GetReviewChangelistsOptions;
import com.perforce.p4java.option.server.GetReviewsOptions;
import com.perforce.p4java.option.server.GetRevisionHistoryOptions;
import com.perforce.p4java.option.server.GetServerProcessesOptions;
import com.perforce.p4java.option.server.GetStreamOptions;
import com.perforce.p4java.option.server.GetStreamsOptions;
import com.perforce.p4java.option.server.GetSubmittedIntegrationsOptions;
import com.perforce.p4java.option.server.GetUserGroupsOptions;
import com.perforce.p4java.option.server.GetUsersOptions;
import com.perforce.p4java.option.server.GraphCommitLogOptions;
import com.perforce.p4java.option.server.GraphReceivePackOptions;
import com.perforce.p4java.option.server.GraphRevListOptions;
import com.perforce.p4java.option.server.GraphShowRefOptions;
import com.perforce.p4java.option.server.JournalWaitOptions;
import com.perforce.p4java.option.server.KeyOptions;
import com.perforce.p4java.option.server.ListOptions;
import com.perforce.p4java.option.server.LogTailOptions;
import com.perforce.p4java.option.server.Login2Options;
import com.perforce.p4java.option.server.LoginOptions;
import com.perforce.p4java.option.server.MatchingLinesOptions;
import com.perforce.p4java.option.server.MoveFileOptions;
import com.perforce.p4java.option.server.ObliterateFilesOptions;
import com.perforce.p4java.option.server.OpenedFilesOptions;
import com.perforce.p4java.option.server.PropertyOptions;
import com.perforce.p4java.option.server.ReloadOptions;
import com.perforce.p4java.option.server.ReposOptions;
import com.perforce.p4java.option.server.SearchJobsOptions;
import com.perforce.p4java.option.server.SetFileAttributesOptions;
import com.perforce.p4java.option.server.StreamIntegrationStatusOptions;
import com.perforce.p4java.option.server.StreamOptions;
import com.perforce.p4java.option.server.StreamlogOptions;
import com.perforce.p4java.option.server.SwitchClientViewOptions;
import com.perforce.p4java.option.server.TagFilesOptions;
import com.perforce.p4java.option.server.UnloadOptions;
import com.perforce.p4java.option.server.UpdateClientOptions;
import com.perforce.p4java.option.server.UpdateUserGroupOptions;
import com.perforce.p4java.option.server.UpdateUserOptions;
import com.perforce.p4java.option.server.VerifyFilesOptions;
import com.perforce.p4java.server.CustomSpec;
import com.perforce.p4java.server.HelixCommandExecutor;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServerAddress;
import com.perforce.p4java.server.IServerAddress.Protocol;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.p4java.server.P4Charset;
import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.p4java.server.ServerStatus;
import com.perforce.p4java.server.callback.DefaultBrowserCallback;
import com.perforce.p4java.server.callback.IBrowserCallback;
import com.perforce.p4java.server.callback.ICommandCallback;
import com.perforce.p4java.server.callback.IProgressCallback;
import com.perforce.p4java.server.callback.ISSOCallback;
import com.perforce.p4java.server.callback.IStreamingCallback;
import com.perforce.p4java.server.delegator.IAttributeDelegator;
import com.perforce.p4java.server.delegator.IBranchDelegator;
import com.perforce.p4java.server.delegator.IBranchesDelegator;
import com.perforce.p4java.server.delegator.IChangeDelegator;
import com.perforce.p4java.server.delegator.IChangesDelegator;
import com.perforce.p4java.server.delegator.IClientDelegator;
import com.perforce.p4java.server.delegator.IClientsDelegator;
import com.perforce.p4java.server.delegator.ICommitDelegator;
import com.perforce.p4java.server.delegator.IConfigureDelegator;
import com.perforce.p4java.server.delegator.ICounterDelegator;
import com.perforce.p4java.server.delegator.ICountersDelegator;
import com.perforce.p4java.server.delegator.IDBSchemaDelegator;
import com.perforce.p4java.server.delegator.IDepotDelegator;
import com.perforce.p4java.server.delegator.IDepotsDelegator;
import com.perforce.p4java.server.delegator.IDiff2Delegator;
import com.perforce.p4java.server.delegator.IDirsDelegator;
import com.perforce.p4java.server.delegator.IDiskspaceDelegator;
import com.perforce.p4java.server.delegator.IDuplicateDelegator;
import com.perforce.p4java.server.delegator.IExportDelegator;
import com.perforce.p4java.server.delegator.IExtensionDelegator;
import com.perforce.p4java.server.delegator.IFileAnnotateDelegator;
import com.perforce.p4java.server.delegator.IFileLogDelegator;
import com.perforce.p4java.server.delegator.IFilesDelegator;
import com.perforce.p4java.server.delegator.IFixDelegator;
import com.perforce.p4java.server.delegator.IFixesDelegator;
import com.perforce.p4java.server.delegator.IFstatDelegator;
import com.perforce.p4java.server.delegator.IGraphListTreeDelegator;
import com.perforce.p4java.server.delegator.IGraphReceivePackDelegator;
import com.perforce.p4java.server.delegator.IGraphRevListDelegator;
import com.perforce.p4java.server.delegator.IGraphShowRefDelegator;
import com.perforce.p4java.server.delegator.IGrepDelegator;
import com.perforce.p4java.server.delegator.IInfoDelegator;
import com.perforce.p4java.server.delegator.IIntegratedDelegator;
import com.perforce.p4java.server.delegator.IJobDelegator;
import com.perforce.p4java.server.delegator.IJobSpecDelegator;
import com.perforce.p4java.server.delegator.IJobsDelegator;
import com.perforce.p4java.server.delegator.IJournalWaitDelegator;
import com.perforce.p4java.server.delegator.IKeyDelegator;
import com.perforce.p4java.server.delegator.IKeysDelegator;
import com.perforce.p4java.server.delegator.ILabelDelegator;
import com.perforce.p4java.server.delegator.ILabelsDelegator;
import com.perforce.p4java.server.delegator.ILicenseDelegator;
import com.perforce.p4java.server.delegator.ILogTailDelegator;
import com.perforce.p4java.server.delegator.ILogin2Delegator;
import com.perforce.p4java.server.delegator.ILoginDelegator;
import com.perforce.p4java.server.delegator.ILogoutDelegator;
import com.perforce.p4java.server.delegator.IMonitorDelegator;
import com.perforce.p4java.server.delegator.IMoveDelegator;
import com.perforce.p4java.server.delegator.IObliterateDelegator;
import com.perforce.p4java.server.delegator.IOpenedDelegator;
import com.perforce.p4java.server.delegator.IPasswdDelegator;
import com.perforce.p4java.server.delegator.IPrintDelegator;
import com.perforce.p4java.server.delegator.IPropertyDelegator;
import com.perforce.p4java.server.delegator.IProtectDelegator;
import com.perforce.p4java.server.delegator.IProtectsDelegator;
import com.perforce.p4java.server.delegator.IReloadDelegator;
import com.perforce.p4java.server.delegator.IRenameUserDelegator;
import com.perforce.p4java.server.delegator.IRenameClientDelegator;
import com.perforce.p4java.server.delegator.IReposDelegator;
import com.perforce.p4java.server.delegator.IReviewDelegator;
import com.perforce.p4java.server.delegator.IReviewsDelegator;
import com.perforce.p4java.server.delegator.ISearchDelegator;
import com.perforce.p4java.server.delegator.ISizesDelegator;
import com.perforce.p4java.server.delegator.ISpecDelegator;
import com.perforce.p4java.server.delegator.IStatDelegator;
import com.perforce.p4java.server.delegator.IStreamDelegator;
import com.perforce.p4java.server.delegator.IStreamlogDelegator;
import com.perforce.p4java.server.delegator.IStreamsDelegator;
import com.perforce.p4java.server.delegator.ITagDelegator;
import com.perforce.p4java.server.delegator.ITriggersDelegator;
import com.perforce.p4java.server.delegator.IUnloadDelegator;
import com.perforce.p4java.server.delegator.IUserDelegator;
import com.perforce.p4java.server.delegator.IUsersDelegator;
import com.perforce.p4java.server.delegator.IVerifyDelegator;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.perforce.p4java.PropertyDefs.AUTO_CONNECT_KEY;
import static com.perforce.p4java.PropertyDefs.AUTO_CONNECT_KEY_SHORTFORM;
import static com.perforce.p4java.PropertyDefs.AUTO_LOGIN_KEY;
import static com.perforce.p4java.PropertyDefs.AUTO_LOGIN_KEY_SHORTFORM;
import static com.perforce.p4java.PropertyDefs.CLIENT_NAME_KEY;
import static com.perforce.p4java.PropertyDefs.CLIENT_NAME_KEY_SHORTFORM;
import static com.perforce.p4java.PropertyDefs.CLIENT_PATH_KEY;
import static com.perforce.p4java.PropertyDefs.CLIENT_PATH_KEY_SHORTFORM;
import static com.perforce.p4java.PropertyDefs.CLIENT_UNSET_NAME_DEFAULT;
import static com.perforce.p4java.PropertyDefs.ENABLE_ANDMAPS;
import static com.perforce.p4java.PropertyDefs.ENABLE_ANDMAPS_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.ENABLE_GRAPH;
import static com.perforce.p4java.PropertyDefs.ENABLE_GRAPH_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.ENABLE_PROGRESS;
import static com.perforce.p4java.PropertyDefs.ENABLE_PROGRESS_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.ENABLE_STREAMS;
import static com.perforce.p4java.PropertyDefs.ENABLE_STREAMS_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.ENABLE_TRACKING;
import static com.perforce.p4java.PropertyDefs.ENABLE_TRACKING_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.IGNORE_FILE_NAME_KEY;
import static com.perforce.p4java.PropertyDefs.IGNORE_FILE_NAME_KEY_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.NON_CHECKED_SYNC;
import static com.perforce.p4java.PropertyDefs.NON_CHECKED_SYNC_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.P4JAVA_TMP_DIR_KEY;
import static com.perforce.p4java.PropertyDefs.PASSWORD_KEY;
import static com.perforce.p4java.PropertyDefs.PASSWORD_KEY_SHORTFORM;
import static com.perforce.p4java.PropertyDefs.QUIET_MODE;
import static com.perforce.p4java.PropertyDefs.QUIET_MODE_SHORT_FORM;
import static com.perforce.p4java.PropertyDefs.USER_NAME_KEY;
import static com.perforce.p4java.PropertyDefs.USER_NAME_KEY_SHORTFORM;
import static com.perforce.p4java.PropertyDefs.USE_AUTH_MEMORY_STORE_KEY;
import static com.perforce.p4java.PropertyDefs.USE_AUTH_MEMORY_STORE_KEY_SHORT_FORM;
import static com.perforce.p4java.common.base.P4JavaExceptions.throwConnectionExceptionIfConditionFails;
import static com.perforce.p4java.common.base.P4JavaExceptions.throwP4JavaErrorIfConditionFails;
import static com.perforce.p4java.core.file.FileSpecOpStatus.VALID;
import static com.perforce.p4java.env.PerforceEnvironment.getP4Charset;
import static com.perforce.p4java.env.PerforceEnvironment.getP4Client;
import static com.perforce.p4java.env.PerforceEnvironment.getP4User;
import static com.perforce.p4java.server.PerforceCharsets.getP4CharsetName;
import static com.perforce.p4java.util.PropertiesHelper.getPropertyByKeys;
import static com.perforce.p4java.util.PropertiesHelper.isExistProperty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Generic abstract superclass for implementation-specific server
 * implementations that use a command-style server interface implementation.
 * <p>
 * Normal users should not be creating this class or subclasses of this class
 * directly; you should use the ServerFactory server factory methods to get a
 * suitable server implementation class.
 */
public abstract class Server extends HelixCommandExecutor implements IServerControl, IOptionsServer {
	// The _FIELD_NAME names below MUST correspond to the names of the
	// static fields used in the individual server impl classes; those
	// fields MUST also be static...
	public static final String SCREEN_NAME_FIELD_NAME = "SCREEN_NAME";
	public static final String IMPL_COMMENTS_FIELD_NAME = "IMPL_COMMENTS";
	public static final String IMPL_TYPE_FIELD_NAME = "IMPL_TYPE";
	public static final String MINIMUM_SUPPORTED_SERVER_LEVEL_FIELD_NAME = "MINIMUM_SUPPORTED_SERVER_LEVEL";
	public static final String PROTOCOL_NAME_FIELD_NAME = "PROTOCOL_NAME";
	public static final String DEFAULT_STATUS_FIELD_NAME = "DEFAULT_STATUS";

	// Prefix used for the (anomalous) setFileAttributes stream map:
	public static final String P4TICKETS_ENV_VAR = "P4TICKETS";
	public static final String P4TICKETS_DEFAULT_WINDOWS = "p4tickets.txt";
	public static final String P4TICKETS_DEFAULT_OTHER = ".p4tickets";
	public static final String P4TRUST_ENV_VAR = "P4TRUST";
	public static final String P4TRUST_DEFAULT_WINDOWS = "p4trust.txt";
	public static final String P4TRUST_DEFAULT_OTHER = ".p4trust";
	public static final String P4IGNORE_ENV_VAR = "P4IGNORE";

	/**
	 * Signals access (login) needed
	 */
	protected static final String CORE_AUTH_FAIL_STRING_1 = "Perforce password (P4PASSWD)";

	/**
	 * Signals access (login) needed
	 */
	protected static final String CORE_AUTH_FAIL_STRING_2 = "Access for user";

	/**
	 * Signals ticket has expired
	 */
	protected static final String CORE_AUTH_FAIL_STRING_3 = "Your session has expired";

	/**
	 * Signals ticket has expired
	 */
	protected static final String CORE_AUTH_FAIL_STRING_4 = "Your session was logged out";

	protected static final int UNKNOWN_SERVER_VERSION = -1;
	protected static final String UNKNOWN_SERVER_HOST = null;
	protected static final int UNKNOWN_SERVER_PORT = -1;

	protected static boolean runningOnWindows = SystemInfo.isWindows();

	protected UsageOptions usageOptions = null;

	protected ServerStatus status = ServerStatus.UNKNOWN;
	protected Properties props = null;

	protected IServerInfo serverInfo = null;
	protected String serverAddress = null;

	protected boolean caseSensitive = true;
	protected int serverVersion = UNKNOWN_SERVER_VERSION;
	protected String serverHost = UNKNOWN_SERVER_HOST;
	protected int serverPort = UNKNOWN_SERVER_PORT;
	protected Protocol serverProtocol = null;

	protected String userName = null;
	protected String password = null;

	/**
	 * Storage for user auth tickets. What's returned from p4 login -p command,
	 * and what we can add to each command when non-null to authenticate it
	 */
	protected Map<String, String> authTickets = new ConcurrentHashMap<>();

	protected IClient client = null;
	protected String clientName = null;
	protected String clientPath = null;

	/**
	 * Used when we have no client set.
	 */
	protected String clientUnsetName = CLIENT_UNSET_NAME_DEFAULT;

	protected boolean setupOnConnect = false;
	protected boolean loginOnConnect = false;

	protected ICommandCallback commandCallback = null;
	protected IProgressCallback progressCallback = null;
	protected ISSOCallback ssoCallback = null;
	protected IBrowserCallback browserCallback = new DefaultBrowserCallback();
	protected String ssoKey = null;

	protected String charsetName = null;
	protected P4Charset p4Charset = null;

	protected boolean connected = false;

	protected int minimumSupportedServerVersion = Metadata.DEFAULT_MINIMUM_SUPPORTED_SERVER_VERSION;

	protected String tmpDirName = null;

	protected AtomicInteger nextCmdCallBackKey = new AtomicInteger();
	protected AtomicInteger nextProgressCallbackKey = new AtomicInteger();

	protected boolean nonCheckedSyncs = false;

	protected boolean enableStreams = true;

	protected boolean enableAndmaps = false;

	protected boolean enableGraph = false;

	protected boolean enableTracking = false;

	protected boolean enableProgress = false;

	protected boolean quietMode = false;

	protected boolean secure = false;

	protected boolean useAuthMemoryStore = false;

	protected String ignoreFileName = null;

	protected String rsh = null;

	protected Object browserVersion = null;

	// The delegators for running perforce commands
	private IAttributeDelegator attributeDelegator = null;
	private IBranchDelegator branchDelegator = null;
	private IBranchesDelegator branchesDelegator = null;
	private IChangeDelegator changeDelegator = null;
	private IChangesDelegator changesDelegator = null;
	private IClientDelegator clientDelegator = null;
	private IClientsDelegator clientsDelegator = null;
	private IConfigureDelegator configureDelegator = null;
	private ICounterDelegator counterDelegator = null;
	private ICountersDelegator countersDelegator = null;
	private IDBSchemaDelegator dbSchemaDelegator = null;
	private IDepotDelegator depotDelegator = null;
	private IDepotsDelegator depotsDelegator = null;
	private IReposDelegator reposDelegator = null;
	private DescribeDelegator describeDelegator = null;
	private IDiff2Delegator diff2Delegator = null;
	private IDirsDelegator dirsDelegator = null;
	private IDiskspaceDelegator diskspaceDelegator = null;
	private IDuplicateDelegator duplicateDelegator = null;
	private IExportDelegator exportDelegator = null;
	private IFileAnnotateDelegator fileAnnotateDelegator = null;
	private IFileLogDelegator fileLogDelegator = null;
	private IFilesDelegator filesDelegator = null;
	private IFixDelegator fixDelegator = null;
	private IFixesDelegator fixesDelegator = null;
	private IFstatDelegator fstatDelegator = null;
	private IGrepDelegator grepDelegator = null;
	private GroupDelegator groupDelegator = null;
	private GroupsDelegator groupsDelegator = null;
	private IInfoDelegator infoDelegator = null;
	private IIntegratedDelegator integratedDelegator = null;
	private InterchangesDelegator interchangesDelegator = null;
	private IJobDelegator jobDelegator = null;
	private IJobsDelegator jobsDelegator = null;
	private IJobSpecDelegator jobSpecDelegator = null;
	private IKeyDelegator keyDelegator = null;
	private IKeysDelegator keysDelegator = null;
	private ILabelDelegator labelDelegator = null;
	private ILabelsDelegator labelsDelegator = null;
	private IMonitorDelegator monitorDelegator = null;
	private IMoveDelegator moveDelegator = null;
	private IStatDelegator statDelegator = null;
	private IJournalWaitDelegator journalWaitDelegator = null;
	private ILoginDelegator loginDelegator;
	private ILogin2Delegator login2Delegator;
	private ILogoutDelegator logoutDelegator;
	private ILogTailDelegator logTailDelegator;
	private IObliterateDelegator obliterateDelegator;
	private IOpenedDelegator openedDelegator;
	private IPasswdDelegator passwdDelegator;
	private IPrintDelegator printDelegator;
	private IPropertyDelegator propertyDelegator;
	private IProtectDelegator protectDelegator;
	private IProtectsDelegator protectsDelegator;
	private IReloadDelegator reloadDelegator;
	private IRenameUserDelegator renameUserDelegator;
	private IReviewDelegator reviewDelegator;
	private IReviewsDelegator reviewsDelegator;
	private ISearchDelegator searchDelegator;
	private ISizesDelegator sizesDelegator;
	private IStreamDelegator streamDelegator;
	private IStreamsDelegator streamsDelegator;
	private ITagDelegator tagDelegator;
	private ITriggersDelegator triggersDelegator;
	private IUnloadDelegator unloadDelegator;
	private IUserDelegator userDelegator;
	private IUsersDelegator usersDelegator;
	private IVerifyDelegator verifyDelegator;
	private IGraphListTreeDelegator graphListTreeDelegator;
	private ICommitDelegator graphCommitDelegator;
	private IGraphRevListDelegator graphRevListDelegator;
	private IGraphReceivePackDelegator graphReceivePackDelegator;
	private IListDelegator listDelegator;
	private IGraphShowRefDelegator graphShowRefDelegator;
	private ISpecDelegator specDelegator;
	private ILicenseDelegator licenseDelegator;
	private IExtensionDelegator extensionDelegator;
	private IStreamlogDelegator streamlogDelegator;
	private IRenameClientDelegator renameClientDelegator;

	/**
	 * Useful source of random integers, etc.
	 */
	protected Random rand = new Random(System.currentTimeMillis());

	public static String guardNull(String str) {
		final String nullStr = "<null>";

		return (str == null ? nullStr : str);
	}

	public static String[] getPreferredPathArray(final String[] preamble, final List<IFileSpec> specList) {
		return getPreferredPathArray(preamble, specList, true);
	}

	public static String[] getPreferredPathArray(final String[] preamble, final List<IFileSpec> specList, final boolean annotate) {
		int pathArraySize = (isNull(preamble) ? 0 : preamble.length) + (isNull(specList) ? 0 : specList.size());
		String[] pathArray = new String[pathArraySize];
		int i = 0;
		if (nonNull(preamble)) {
			for (String str : preamble) {
				pathArray[i++] = str;
			}
		}

		if (nonNull(specList)) {
			for (IFileSpec fSpec : specList) {
				if (nonNull(fSpec) && (fSpec.getOpStatus() == VALID)) {
					if (annotate) {
						pathArray[i++] = fSpec.getAnnotatedPreferredPathString();
					} else {
						pathArray[i++] = fSpec.getPreferredPathString();
					}
				} else {
					pathArray[i++] = null;
				}
			}
		}

		return pathArray;
	}

	public static String[] populatePathArray(final String[] pathArray, final int start, final List<IFileSpec> fileSpecList) {

		if (isNull(pathArray)) {
			return null;
		}
		if (isNull(fileSpecList)) {
			return pathArray;
		}

		throwP4JavaErrorIfConditionFails(start >= 0, "negative start index in populatePathArray: %s", start);
		throwP4JavaErrorIfConditionFails(start <= pathArray.length && (start + fileSpecList.size() <= pathArray.length), "pathArray too small in populatePathArray");

		int i = start;
		for (IFileSpec fSpec : fileSpecList) {
			if (nonNull(fSpec) && (fSpec.getOpStatus() == VALID)) {
				pathArray[i] = fSpec.getAnnotatedPreferredPathString();
			} else {
				pathArray[i] = null;
			}
			i++;
		}

		return pathArray;
	}

	/**
	 * @return true if the JVM indicates that we're running on a Windows
	 * platform. Not entirely reliable, but good enough for our purposes.
	 */
	public static boolean isRunningOnWindows() {
		return runningOnWindows;
	}

	@Override
	public String getCharsetName() {
		return charsetName;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientPath() {
		return clientPath;
	}

	public void setClientPath(String clientPath) {
		this.clientPath = clientPath;
	}

	public String getIgnoreFileName() {
		return ignoreFileName;
	}

	public void setIgnoreFileName(String ignoreFileName) {
		this.ignoreFileName = ignoreFileName;
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	public UsageOptions getUsageOptions() {
		return usageOptions;
	}

	public Server setUsageOptions(UsageOptions usageOptions) {
		this.usageOptions = usageOptions;
		return this;
	}

	/**
	 * Returns a list of revisions given the options
	 *
	 * @param options graph Revision List Options
	 * @return list of graph commits
	 * @throws P4JavaException API errors
	 */
	@Override
	public List<IRevListCommit> getGraphRevList(GraphRevListOptions options) throws P4JavaException {
		return graphRevListDelegator.getGraphRevList(options);
	}

	@Override
	public List<ICommit> getGraphCommitLogList(GraphCommitLogOptions options) throws P4JavaException {
		return graphCommitDelegator.getGraphCommitLogList(options);
	}

	@Override
	public void doGraphReceivePack(GraphReceivePackOptions options) throws P4JavaException {
		graphReceivePackDelegator.doGraphReceivePack(options);
	}

	/**
	 * Usage: list [-l label [-d]] [-C] [-M] files..
	 *
	 * @param fileSpecs List of file specs
	 * @param options   Command options
	 * @return ListData
	 * @throws P4JavaException API errors
	 */
	@Override
	public ListData getListData(List<IFileSpec> fileSpecs, ListOptions options) throws P4JavaException {
		return listDelegator.getListData(fileSpecs, options);
	}

	/**
	 * Internal use only.
	 * <p>
	 * Use IClient.getListData to restrict list to client view.
	 *
	 * @param fileSpecs List of file specs
	 * @param options   Command options
	 * @return ListData
	 * @throws P4JavaException API errors
	 */
	@Override
	public ListData getListData(List<IFileSpec> fileSpecs, ListOptions options, String clientName) throws P4JavaException {
		return listDelegator.getListData(fileSpecs, options, clientName);
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public void setUserName(String userName) {
		this.userName = userName;
		setAuthTicket(getAuthTicket(userName));
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	public boolean isEnableProgress() {
		return enableProgress;
	}

	public void setEnableProgress(boolean enableProgress) {
		this.enableProgress = enableProgress;
	}

	public boolean isEnableTracking() {
		return enableTracking;
	}

	public void setEnableTracking(boolean enableTracking) {
		this.enableTracking = enableTracking;
	}

	public boolean isNonCheckedSyncs() {
		return nonCheckedSyncs;
	}

	public void setNonCheckedSyncs(boolean nonCheckedSyncs) {
		this.nonCheckedSyncs = nonCheckedSyncs;
	}

	public boolean isQuietMode() {
		return quietMode;
	}

	public void setQuietMode(boolean quietMode) {
		this.quietMode = quietMode;
	}

	/**
	 * Check if the server is secure (SSL) or not.
	 *
	 * @return if secure
	 */
	protected boolean isSecure() {
		return secure;
	}

	/**
	 * Sets the server to secure (SSL) or non-secure mode.
	 *
	 * @param secure secure
	 */
	protected void setSecure(boolean secure) {
		this.secure = secure;
	}

	@Override
	public int getServerVersion() throws ConnectionException {
		if (serverVersion != UNKNOWN_SERVER_VERSION) {
			// We've already got the server version. This will fail
			// if the server changes underneath us, but that's life...
			return serverVersion;
		}

		try {
			serverInfo = getServerInfo();
			if (nonNull(serverInfo)) {
				if (isNotBlank(serverInfo.getServerAddress())) {
					serverAddress = serverInfo.getServerAddress();
				}
				String currentServerVersion = serverInfo.getServerVersion();
				if (isNotBlank(currentServerVersion)) {
					serverVersion = parseVersionString(currentServerVersion);
					return serverVersion;
				}
			}
		} catch (Exception exc) {
			Log.exception(exc);
			throw new ConnectionException(exc.getLocalizedMessage(), exc);
		}
		return UNKNOWN_SERVER_VERSION;
	}

	@Override
	public void setCurrentServerInfo(IServerInfo info) {
		this.serverInfo = info;
	}

	@Override
	public IServerInfo getCurrentServerInfo() {
		return this.serverInfo;
	}

	@Override
	public void connect() throws ConnectionException, AccessException, RequestException, ConfigException {
		connected = true;
		status = ServerStatus.READY;

		Log.info("connected to Perforce server at %s:%s", serverHost, serverPort);

		// Try to get and then verify the server version:
		int serverVersion = getServerVersion();
		throwConnectionExceptionIfConditionFails(serverVersion != UNKNOWN_SERVER_VERSION, "Unable to determine Perforce server version for connection; " + "check network connection, connection character set setting, " + "and / or server status");

		throwConnectionExceptionIfConditionFails(serverVersion >= minimumSupportedServerVersion, "Attempted to connect to an unsupported Perforce server version; " + "target server version: %s; minimum supported version: %s", serverVersion, minimumSupportedServerVersion);

		if (loginOnConnect && isNotBlank(userName) && isNotBlank(password)) {
			login(password);
		}

		if (setupOnConnect && isNotBlank(clientName)) {
			// Attempt to get the client set up, etc.; subclasses will
			// probably do much more than this, or nothing at all...
			client = getClient(clientName);
		}

		// If the charset is not set and P4CHARSET is null/none/auto (unset),
		// automatically sets it to the Java default charset.
		// The following are special cases.
		// "auto" (Guess a P4CHARSET based on client OS params)
		// "none" (same as unsetting P4CHARSET)
		if (serverInfo.isUnicodeEnabled() && (p4Charset == null || p4Charset.getCharset() == null)) {
			String p4CharsetStr = getP4Charset();
			if (isBlank(p4CharsetStr) || "none".equalsIgnoreCase(p4CharsetStr) || "auto".equalsIgnoreCase(p4CharsetStr)) {
				// Get the first matching Perforce charset for the Java default
				// charset
				String p4CharsetName = getP4CharsetName(CharsetDefs.DEFAULT_NAME);
				if (isNotBlank(p4CharsetName)) {
					charsetName = p4CharsetName;
					p4Charset = P4Charset.getDefault();
				} else { // Default to Perforce "utf8" equivalent to Java
					// "UTF-8"
					charsetName = "utf8";
					this.p4Charset = P4Charset.getUTF8();
				}
			} else {
				setCharsetName(p4CharsetStr);
			}
		}
	}

	@Override
	public void disconnect() throws ConnectionException, AccessException {
		connected = false;
		status = ServerStatus.DISCONNECTED;
		Log.info("disconnected from Perforce server at %s:%s", serverHost, serverPort);
	}

	@Override
	public String getAuthTicket() {
		return getAuthTicket(userName);
	}

	@Override
	public void setAuthTicket(String authTicket) {
		if (isNotBlank(userName)) {
			setAuthTicket(userName, authTicket);
		}
	}

	@Override
	public String[] getKnownCharsets() {
		return PerforceCharsets.getKnownCharsets();
	}

	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public int getServerVersionNumber() {
		return serverVersion;
	}

	public String getWorkingDirectory() {
		if (nonNull(usageOptions)) {
			return usageOptions.getWorkingDirectory();
		} else {
			return null;
		}
	}

	public void setWorkingDirectory(final String dirPath) {
		if (nonNull(usageOptions)) {
			usageOptions.setWorkingDirectory(dirPath);
		}
	}

	@Override
	public IClient getCurrentClient() {
		return client;
	}

	@Override
	public void setCurrentClient(final IClient client) {
		this.client = client;

		if (nonNull(client)) {
			clientName = client.getName();
		} else {
			clientName = null;
		}
	}

	@Override
	public ICommandCallback registerCallback(ICommandCallback callback) {
		ICommandCallback oldCallback = commandCallback;
		commandCallback = callback;
		return oldCallback;
	}

	@Override
	public IProgressCallback registerProgressCallback(IProgressCallback progressCallback) {
		IProgressCallback oldCallback = this.progressCallback;
		this.progressCallback = progressCallback;
		return oldCallback;
	}

	@Override
	public void registerSSOCallback(ISSOCallback callback, String ssoKey) {
		ssoCallback = callback;
		this.ssoKey = ssoKey;
	}

	@Override
	public void registerBrowserCallback(IBrowserCallback browserCallback) {
		this.browserCallback = browserCallback;
	}

	@Override
	public boolean setCharsetName(final String charsetName) throws UnsupportedCharsetException {
		if (charsetName == null) {
			this.p4Charset = null;
			this.charsetName = null;
			return true;
		}
		this.p4Charset = new P4Charset(charsetName);
		this.charsetName = p4Charset.getCharsetName();
		return nonNull(p4Charset.getCharset());
	}

	@Override
	public boolean supportsUnicode() throws ConnectionException, RequestException, AccessException {
		if (isNull(serverInfo)) {
			serverInfo = getServerInfo();
		}

		return nonNull(serverInfo) && serverInfo.isUnicodeEnabled();
	}

	@Override
	public ServerStatus init(final String host, final int port, final Properties properties, final UsageOptions opts, final boolean secure) throws ConfigException, ConnectionException {
		serverHost = host;
		serverPort = port;
		this.secure = secure;

		props = ObjectUtils.firstNonNull(properties, new Properties());
		usageOptions = ObjectUtils.firstNonNull(opts, new UsageOptions(this.props));

		// Retrieve some fairly generic properties; note the use of the short
		// form keys for
		// program name and version (done as a favour to testers and users
		// everywhere...).
		tmpDirName = RpcPropertyDefs.getProperty(props, P4JAVA_TMP_DIR_KEY, System.getProperty("java.io.tmpdir"));

		if (isBlank(tmpDirName)) {
			// This can really only happen if someone has nuked or played with
			// the JVM's system properties before we get here... the default
			// will
			// work for most non-Windows boxes in most cases, and may not be
			// needed in many cases anyway.
			tmpDirName = "/tmp";
			Log.warn("Unable to get tmp name from P4 properties or System; using %s instead", tmpDirName);
		}

		Log.info("Using program name: '%s'; program version: '%s'", usageOptions.getProgramName(), usageOptions.getProgramVersion());
		Log.info("Using tmp file directory: %s", tmpDirName);

		setUserName(getPropertyByKeys(props, USER_NAME_KEY_SHORTFORM, USER_NAME_KEY, getP4User()));
		password = getPropertyByKeys(props, PASSWORD_KEY_SHORTFORM, PASSWORD_KEY, null);
		clientName = getPropertyByKeys(props, CLIENT_NAME_KEY_SHORTFORM, CLIENT_NAME_KEY, getP4Client());
		clientPath = getPropertyByKeys(props, CLIENT_PATH_KEY_SHORTFORM, CLIENT_PATH_KEY, null);

		setupOnConnect = isExistProperty(props, AUTO_CONNECT_KEY_SHORTFORM, AUTO_CONNECT_KEY, setupOnConnect);
		loginOnConnect = isExistProperty(props, AUTO_LOGIN_KEY_SHORTFORM, AUTO_LOGIN_KEY, loginOnConnect);
		nonCheckedSyncs = isExistProperty(props, NON_CHECKED_SYNC_SHORT_FORM, NON_CHECKED_SYNC, nonCheckedSyncs);
		enableStreams = isExistProperty(props, ENABLE_STREAMS_SHORT_FORM, ENABLE_STREAMS, enableStreams);
		enableAndmaps = isExistProperty(props, ENABLE_ANDMAPS_SHORT_FORM, ENABLE_ANDMAPS, enableAndmaps);
		enableGraph = isExistProperty(props, ENABLE_GRAPH_SHORT_FORM, ENABLE_GRAPH, enableGraph);
		enableTracking = isExistProperty(props, ENABLE_TRACKING_SHORT_FORM, ENABLE_TRACKING, enableTracking);
		enableProgress = isExistProperty(props, ENABLE_PROGRESS_SHORT_FORM, ENABLE_PROGRESS, enableProgress);
		quietMode = isExistProperty(props, QUIET_MODE_SHORT_FORM, QUIET_MODE, quietMode);
		useAuthMemoryStore = isExistProperty(props, USE_AUTH_MEMORY_STORE_KEY_SHORT_FORM, USE_AUTH_MEMORY_STORE_KEY, useAuthMemoryStore);

		// Attempt to get the P4IGNORE file name from the passed-in properties
		// or the system environment variable 'P4IGNORE'
		ignoreFileName = getPropertyByKeys(props, IGNORE_FILE_NAME_KEY_SHORT_FORM, IGNORE_FILE_NAME_KEY, System.getenv(P4IGNORE_ENV_VAR));
		// Instantiate the delegators
		attributeDelegator = new AttributeDelegator(this);
		branchDelegator = new BranchDelegator(this);
		branchesDelegator = new BranchesDelegator(this);
		changeDelegator = new ChangeDelegator(this);
		changesDelegator = new ChangesDelegator(this);
		clientDelegator = new ClientDelegator(this);
		clientsDelegator = new ClientsDelegator(this);
		configureDelegator = new ConfigureDelegator(this);
		counterDelegator = new CounterDelegator(this);
		countersDelegator = new CountersDelegator(this);
		dbSchemaDelegator = new DBSchemaDelegator(this);
		depotDelegator = new DepotDelegator(this);
		depotsDelegator = new DepotsDelegator(this);
		reposDelegator = new ReposDelegator(this);
		describeDelegator = new DescribeDelegator(this);
		diff2Delegator = new Diff2Delegator(this);
		dirsDelegator = new DirsDelegator(this);
		diskspaceDelegator = new DiskspaceDelegator(this);
		duplicateDelegator = new DuplicateDelegator(this);
		exportDelegator = new ExportDelegator(this);
		fileAnnotateDelegator = new FileAnnotateDelegator(this);
		fileLogDelegator = new FileLogDelegator(this);
		filesDelegator = new FilesDelegator(this);
		fixDelegator = new FixDelegator(this);
		fixesDelegator = new FixesDelegator(this);
		fstatDelegator = new FstatDelegator(this);
		grepDelegator = new GrepDelegator(this);
		groupDelegator = new GroupDelegator(this);
		groupsDelegator = new GroupsDelegator(this);
		infoDelegator = new InfoDelegator(this);
		integratedDelegator = new IntegratedDelegator(this);
		interchangesDelegator = new InterchangesDelegator(this);
		jobDelegator = new JobDelegator(this);
		jobsDelegator = new JobsDelegator(this);
		jobSpecDelegator = new JobSpecDelegator(this);
		keyDelegator = new KeyDelegator(this);
		keysDelegator = new KeysDelegator(this);
		labelDelegator = new LabelDelegator(this);
		labelsDelegator = new LabelsDelegator(this);
		monitorDelegator = new MonitorDelegator(this);
		moveDelegator = new MoveDelegator(this);
		statDelegator = new StatDelegator(this);
		journalWaitDelegator = new JournalWaitDelegator(this);
		loginDelegator = new LoginDelegator(this);
		login2Delegator = new Login2Delegator(this);
		logoutDelegator = new LogoutDelegator(this);
		logTailDelegator = new LogTailDelegator(this);
		obliterateDelegator = new ObliterateDelegator(this);
		openedDelegator = new OpenedDelegator(this);
		passwdDelegator = new PasswdDelegator(this);
		printDelegator = new PrintDelegator(this);
		propertyDelegator = new PropertyDelegator(this);
		protectDelegator = new ProtectDelegator(this);
		protectsDelegator = new ProtectsDelegator(this);
		reloadDelegator = new ReloadDelegator(this);
		renameUserDelegator = new RenameUserDelegator(this);
		reviewDelegator = new ReviewDelegator(this);
		reviewsDelegator = new ReviewsDelegator(this);
		searchDelegator = new SearchDelegator(this);
		sizesDelegator = new SizesDelegator(this);
		streamDelegator = new StreamDelegator(this);
		streamsDelegator = new StreamsDelegator(this);
		tagDelegator = new TagDelegator(this);
		triggersDelegator = new TriggersDelegator(this);
		unloadDelegator = new UnloadDelegator(this);
		userDelegator = new UserDelegator(this);
		usersDelegator = new UsersDelegator(this);
		verifyDelegator = new VerifyDelegator(this);
		graphListTreeDelegator = new GraphListTreeDelegator(this);
		graphCommitDelegator = new CommitDelegator(this);
		graphRevListDelegator = new GraphRevListDelegator(this);
		graphReceivePackDelegator = new GraphReceivePackDelegator(this);
		listDelegator = new ListDelegator(this);
		graphShowRefDelegator = new GraphShowRefDelegator(this);
		specDelegator = new SpecDelegator(this);
		licenseDelegator = new LicenseDelegator(this);
		extensionDelegator = new ExtensionDelegator(this);
		streamlogDelegator = new StreamlogDelegator(this);
		renameClientDelegator = new RenameClientDelegator(this);
		return status; // Which is UNKNOWN at this point...
	}

	@Override
	public ServerStatus init(final String host, final int port, final Properties props, final UsageOptions opts) throws ConfigException, ConnectionException {

		return init(host, port, props, opts, false);
	}

	@Override
	public ServerStatus init(final String host, final int port, final Properties props) throws ConfigException, ConnectionException {

		return init(host, port, props, null);
	}

	/**
	 * Get default p4tickets file for the running OS
	 *
	 * @return - default p4tickets file
	 */
	protected String getDefaultP4TicketsFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(SystemInfo.getUserHome()).append(SystemInfo.getFileSeparator());
		if (SystemInfo.isWindows()) {
			sb.append(P4TICKETS_DEFAULT_WINDOWS);
		} else {
			sb.append(P4TICKETS_DEFAULT_OTHER);
		}
		return sb.toString();
	}

	/**
	 * Get default p4trust file for the running OS
	 *
	 * @return - default p4trust file
	 */
	protected String getDefaultP4TrustFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(SystemInfo.getUserHome()).append(SystemInfo.getFileSeparator());
		if (SystemInfo.isWindows()) {
			sb.append(P4TRUST_DEFAULT_WINDOWS);
		} else {
			sb.append(P4TRUST_DEFAULT_OTHER);
		}
		return sb.toString();
	}

	/**
	 * Get the server address entry from the p4 info.
	 *
	 * @return - server address or null if error
	 */
	protected String getInfoServerAddress() {
		if (isNotBlank(serverAddress)) {
			// We've already got the server version. This will fail
			// if the server changes underneath us, but that's life...
			return serverAddress;
		}

		try {
			serverInfo = getServerInfo();
			if (nonNull(serverInfo)) {
				String serverInfoServerAddress = serverInfo.getServerAddress();
				if (isNotBlank(serverInfoServerAddress)) {
					serverAddress = serverInfoServerAddress;
				}

				String serverInfoServerVersion = serverInfo.getServerVersion();
				if (isNotBlank(serverInfoServerVersion)) {
					serverVersion = parseVersionString(serverInfoServerVersion);
				}
			}
		} catch (Exception exc) {
			Log.exception(exc);
		}

		return serverAddress;
	}

	/**
	 * Return the major version number (e.g. 20081) from the passed-in complete
	 * version string. Instead of using regex or anything too complex we just
	 * keep splitting the string and recombining; this could be optimised or
	 * flexibilised fairly easily on one of those long rainy days... (HR).
	 *
	 * @param versionString versionString
	 * @return version
	 */
	protected int parseVersionString(final String versionString) {
		// Format: P4D/LINUX26X86/2007.3/142194 (2007/12/17),
		// but with minor variants possible due to internal server builds,
		// e.g. 2005.2.r05.2_nightly. But all we want is the 2007.3 turned
		// into an int like 20073...

		if (isNotBlank(versionString)) {
			String[] subStrings = versionString.split("/");
			if (subStrings.length >= 3) {
				String candidate = subStrings[2];
				String[] candidateParts = candidate.split("\\.");
				if (candidateParts.length >= 2) {
					try {
						return Integer.parseInt(candidateParts[0] + candidateParts[1]);
					} catch (NumberFormatException nfe) {
						Log.error("Unexpected exception in P4CmdServerImpl.parseVersionString: %s", nfe);
					}
				}
			}
		}

		return UNKNOWN_SERVER_VERSION;
	}

	/**
	 * @return the next positive pseudo random int.
	 */
	protected int getRandomInt() {
		return Math.abs(rand.nextInt(Integer.MAX_VALUE));
	}

	public ISSOCallback getSSOCallback() {
		return ssoCallback;
	}

	public String getSSOKey() {
		return ssoKey;
	}

	public IBrowserCallback getBrowserCallback() {
		return browserCallback;
	}

	protected boolean isUnicode() {
		return isNotBlank(charsetName);
	}

	// Command delegators
	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IAttributeDelegator#
	 * unsetFileAttribute(java.util.List, java.util.Map,
	 * com.perforce.p4java.option.server.SetFileAttributesOptions)
	 */
	@Override
	public List<IFileSpec> unsetFileAttribute(List<IFileSpec> files, String attribute, SetFileAttributesOptions opts) throws P4JavaException {
		return attributeDelegator.unsetFileAttribute(files, attribute, opts);
	}

	// Command delegators
	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IAttributeDelegator#
	 * unsetFileAttributes(java.util.List, java.util.Map,
	 * com.perforce.p4java.option.server.SetFileAttributesOptions)
	 */
	@Override
	public List<IFileSpec> unsetFileAttributes(List<IFileSpec> files, List<String> attributes, SetFileAttributesOptions opts) throws P4JavaException {
		return attributeDelegator.unsetFileAttributes(files, attributes, opts);
	}

	// Command delegators
	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IAttributeDelegator#
	 * setFileAttributes(java.util.List, java.util.Map,
	 * com.perforce.p4java.option.server.SetFileAttributesOptions)
	 */
	@Override
	public List<IFileSpec> setFileAttributes(List<IFileSpec> files, Map<String, String> attributes, SetFileAttributesOptions opts) throws P4JavaException {
		return attributeDelegator.setFileAttributes(files, attributes, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IAttributeDelegator#
	 * setFileAttributes(java.util.List, java.lang.String, java.io.InputStream,
	 * com.perforce.p4java.option.server.SetFileAttributesOptions)
	 */
	@Override
	public List<IFileSpec> setFileAttributes(List<IFileSpec> files, @Nonnull String attributeName, @Nonnull InputStream inStream, SetFileAttributesOptions opts) throws P4JavaException {
		return attributeDelegator.setFileAttributes(files, attributeName, inStream, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.delegator.IBranchesDelegator#getBranchSpecs(
	 * com.perforce.p4java.option.server.GetBranchSpecsOptions)
	 */
	@Override
	public List<IBranchSpecSummary> getBranchSpecs(GetBranchSpecsOptions opts) throws P4JavaException {
		return branchesDelegator.getBranchSpecs(opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getBranchSpecs(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public List<IBranchSpecSummary> getBranchSpecs(String userName, String nameFilter, int maxReturns) throws ConnectionException, RequestException, AccessException {
		return branchesDelegator.getBranchSpecs(userName, nameFilter, maxReturns);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IBranchDelegator#getBranchSpec(java.lang.
	 * String, com.perforce.p4java.option.server.GetBranchSpecOptions)
	 */
	@Override
	public IBranchSpec getBranchSpec(String name, GetBranchSpecOptions opts) throws P4JavaException {
		return branchDelegator.getBranchSpec(name, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.IBranchDelegator#deleteBranchSpec(java.lang.
	 * String, com.perforce.p4java.option.server.DeleteBranchSpecOptions)
	 */
	@Override
	public String deleteBranchSpec(String branchSpecName, DeleteBranchSpecOptions opts) throws P4JavaException {
		return branchDelegator.deleteBranchSpec(branchSpecName, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IBranchDelegator#getBranchSpec(java.lang.
	 * String)
	 */
	@Override
	public IBranchSpec getBranchSpec(String name) throws ConnectionException, RequestException, AccessException {
		return branchDelegator.getBranchSpec(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.IBranchDelegator#createBranchSpec(com.perforce
	 * .p4java.core.IBranchSpec)
	 */
	@Override
	public String createBranchSpec(@Nonnull IBranchSpec branchSpec) throws ConnectionException, RequestException, AccessException {
		return branchDelegator.createBranchSpec(branchSpec);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.IBranchDelegator#updateBranchSpec(com.perforce
	 * .p4java.core.IBranchSpec)
	 */
	@Override
	public String updateBranchSpec(@Nonnull IBranchSpec branchSpec) throws ConnectionException, RequestException, AccessException {
		return branchDelegator.updateBranchSpec(branchSpec);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IBranchDelegator#deleteBranchSpec(java.lang.String, boolean)
	 * @deprecated use {@link IBranchDelegator#deleteBranchSpec(String, DeleteBranchSpecOptions)} instead
	 */
	@Deprecated
	@Override
	public String deleteBranchSpec(String branchSpecName, boolean force) throws ConnectionException, RequestException, AccessException {
		try {
			return branchDelegator.deleteBranchSpec(branchSpecName, new DeleteBranchSpecOptions(force));
		} catch (P4JavaException p4je) {
			throw new RequestException(p4je);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#deletePendingChangelist(int)
	 */
	@Override
	public String deletePendingChangelist(int id) throws ConnectionException, RequestException, AccessException {
		return changeDelegator.deletePendingChangelist(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelist(int)
	 */
	@Override
	public IChangelist getChangelist(int id) throws ConnectionException, RequestException, AccessException {
		return changeDelegator.getChangelist(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IChangeDelegator#
	 * deletePendingChangelist(int,
	 * com.perforce.p4java.option.server.ChangelistOptions)
	 */
	@Override
	public String deletePendingChangelist(int id, ChangelistOptions opts) throws P4JavaException {
		return changeDelegator.deletePendingChangelist(id, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.delegator.IChangeDelegator#getChangelist(int,
	 * com.perforce.p4java.option.server.ChangelistOptions)
	 */
	@Override
	public IChangelist getChangelist(int id, ChangelistOptions opts) throws P4JavaException {
		return changeDelegator.getChangelist(id, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelists(int,
	 * java.util.List, java.lang.String, java.lang.String, boolean,
	 * com.perforce.p4java.core.IChangelist.Type, boolean)
	 */
	@Override
	public List<IChangelistSummary> getChangelists(final int maxMostRecent, final List<IFileSpec> fileSpecs, final String clientName, final String userName, final boolean includeIntegrated, final Type type, final boolean longDesc) throws ConnectionException, RequestException, AccessException {
		return changesDelegator.getChangelists(maxMostRecent, fileSpecs, clientName, userName, includeIntegrated, type, longDesc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelists(int,
	 * java.util.List, java.lang.String, java.lang.String, boolean, boolean,
	 * boolean, boolean)
	 */
	@Override
	public List<IChangelistSummary> getChangelists(final int maxMostRecent, final List<IFileSpec> fileSpecs, final String clientName, final String userName, final boolean includeIntegrated, final boolean submittedOnly, final boolean pendingOnly, final boolean longDesc) throws ConnectionException, RequestException, AccessException {

		return changesDelegator.getChangelists(maxMostRecent, fileSpecs, clientName, userName, includeIntegrated, submittedOnly, pendingOnly, longDesc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.delegator.IChangesDelegator#getChangelists(
	 * java.util.List, com.perforce.p4java.option.server.GetChangelistsOptions)
	 */
	public List<IChangelistSummary> getChangelists(final List<IFileSpec> fileSpecs, final GetChangelistsOptions opts) throws P4JavaException {
		return changesDelegator.getChangelists(fileSpecs, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IDescribeDelegator#
	 * getChangelistDiffs(int,
	 * com.perforce.p4java.option.server.GetChangelistDiffsOptions)
	 */
	@Override
	public InputStream getChangelistDiffs(int id, GetChangelistDiffsOptions opts) throws P4JavaException {
		return describeDelegator.getChangelistDiffs(id, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IDescribeDelegator#
	 * getChangelistDiffsStream(int,
	 * com.perforce.p4java.option.server.DescribeOptions)
	 */
	@Override
	public InputStream getChangelistDiffsStream(int id, DescribeOptions options) throws ConnectionException, RequestException, AccessException {
		return describeDelegator.getChangelistDiffsStream(id, options);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.delegator.IDescribeDelegator#getShelvedFiles(
	 * int)
	 */
	@Override
	public List<IFileSpec> getShelvedFiles(int changelistId) throws P4JavaException {
		return describeDelegator.getShelvedFiles(changelistId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.delegator.IDescribeDelegator#getShelvedFiles(
	 * int, int)
	 */
	@Override
	public List<IFileSpec> getShelvedFiles(int changelistId, int max) throws P4JavaException {
		return describeDelegator.getShelvedFiles(changelistId, max);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelistDiffs(int,
	 * com.perforce.p4java.core.file.DiffType)
	 */
	@Override
	public InputStream getChangelistDiffs(int id, DiffType diffType) throws ConnectionException, RequestException, AccessException {
		return describeDelegator.getChangelistDiffs(id, diffType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelistFiles(int)
	 */
	@Override
	public List<IFileSpec> getChangelistFiles(int id) throws ConnectionException, RequestException, AccessException {
		return describeDelegator.getChangelistFiles(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelistFiles(int, int)
	 */
	@Override
	public List<IFileSpec> getChangelistFiles(int id, int max) throws ConnectionException, RequestException, AccessException {
		return describeDelegator.getChangelistFiles(id, max);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelistExtendedFiles(int)
	 */
	@Override
	public List<IExtendedFileSpec> getChangelistExtendedFiles(int id) throws ConnectionException, RequestException, AccessException {
		return describeDelegator.getChangelistExtendedFiles(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getChangelistExtendedFiles(int, int)
	 */
	@Override
	public List<IExtendedFileSpec> getChangelistExtendedFiles(int id, int max) throws ConnectionException, RequestException, AccessException {
		return describeDelegator.getChangelistExtendedFiles(id, max);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getCommitFiles(String, String)
	 */
	@Override
	public List<IFileSpec> getCommitFiles(final String repo, final String commit) throws ConnectionException, RequestException, AccessException {
		return describeDelegator.getCommitFiles(repo, commit);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IConfigureDelegator#
	 * setOrUnsetServerConfigurationValue(java.lang.String, java.lang.String)
	 */
	@Override
	public String setOrUnsetServerConfigurationValue(@Nonnull final String name, @Nullable final String value) throws P4JavaException {
		return configureDelegator.setOrUnsetServerConfigurationValue(name, value);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.delegator.IConfigureDelegator#
	 * showServerConfiguration(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ServerConfigurationValue> showServerConfiguration(final String serverName, final String variableName) throws P4JavaException {
		return configureDelegator.showServerConfiguration(serverName, variableName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getDbSchema(java.util.List)
	 */
	@Override
	public List<IDbSchema> getDbSchema(List<String> tableSpecs) throws P4JavaException {
		return dbSchemaDelegator.getDbSchema(tableSpecs);
	}

	@Override
	public IClient getClient(String clientName) throws ConnectionException, RequestException, AccessException {
		return clientDelegator.getClient(clientName);
	}

	@Override
	public IClient getClient(@Nonnull IClientSummary clientSummary) throws ConnectionException, RequestException, AccessException {
		return clientDelegator.getClient(clientSummary);
	}

	@Override
	public IClient getClientTemplate(String clientName) throws ConnectionException, RequestException, AccessException {
		return clientDelegator.getClientTemplate(clientName);
	}

	@Override
	public IClient getClientTemplate(String clientName, boolean allowExistent) throws ConnectionException, RequestException, AccessException {
		return clientDelegator.getClientTemplate(clientName, allowExistent);
	}

	@Override
	public IClient getClientTemplate(String clientName, GetClientTemplateOptions opts) throws P4JavaException {
		return clientDelegator.getClientTemplate(clientName, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.IInterchangesDelegator#getInterchanges(com.
	 * perforce.p4java.core.file.IFileSpec,
	 * com.perforce.p4java.core.file.IFileSpec,
	 * com.perforce.p4java.option.server.GetInterchangesOptions)
	 */
	@Override
	public List<IChangelist> getInterchanges(IFileSpec fromFile, IFileSpec toFile, GetInterchangesOptions opts) throws P4JavaException {
		return interchangesDelegator.getInterchanges(fromFile, toFile, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.IInterchangesDelegator#getInterchanges(java.
	 * lang.String, java.util.List, java.util.List,
	 * com.perforce.p4java.option.server.GetInterchangesOptions)
	 */
	@Override
	public List<IChangelist> getInterchanges(String branchSpecName, List<IFileSpec> fromFileList, List<IFileSpec> toFileList, GetInterchangesOptions opts) throws P4JavaException {
		return interchangesDelegator.getInterchanges(branchSpecName, fromFileList, toFileList, opts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.perforce.p4java.server.IServer#getInterchanges(com.perforce.p4java.
	 * core.file.IFileSpec, com.perforce.p4java.core.file.IFileSpec, boolean,
	 * boolean, int)
	 */
	@Override
	public List<IChangelist> getInterchanges(IFileSpec fromFile, IFileSpec toFile, boolean showFiles, boolean longDesc, int maxChangelistId) throws ConnectionException, RequestException, AccessException {
		return interchangesDelegator.getInterchanges(fromFile, toFile, showFiles, longDesc, maxChangelistId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.perforce.p4java.server.IServer#getInterchanges(java.lang.String,
	 * java.util.List, java.util.List, boolean, boolean, int, boolean, boolean)
	 */
	@Override
	public List<IChangelist> getInterchanges(String branchSpecName, List<IFileSpec> fromFileList, List<IFileSpec> toFileList, boolean showFiles, boolean longDesc, int maxChangelistId, boolean reverseMapping, boolean biDirectional) throws ConnectionException, RequestException, AccessException {
		return interchangesDelegator.getInterchanges(branchSpecName, fromFileList, toFileList, showFiles, longDesc, maxChangelistId, reverseMapping, biDirectional);
	}

	@Override
	public String createClient(@Nonnull IClient newClient) throws ConnectionException, RequestException, AccessException {
		return clientDelegator.createClient(newClient);
	}

	@Override
	public void createTempClient(@Nonnull IClient newClient) throws ConnectionException, RequestException, AccessException {
		clientDelegator.createTempClient(newClient);
	}

	@Override
	public String updateClient(@Nonnull IClient client) throws ConnectionException, RequestException, AccessException {
		return clientDelegator.updateClient(client);
	}

	@Override
	public String updateClient(IClient client, boolean force) throws ConnectionException, RequestException, AccessException {
		return clientDelegator.updateClient(client, force);
	}

	@Override
	public String updateClient(IClient client, UpdateClientOptions opts) throws P4JavaException {
		return clientDelegator.updateClient(client, opts);
	}

	@Override
	public String deleteClient(String clientName, boolean force) throws ConnectionException, RequestException, AccessException {

		return clientDelegator.deleteClient(clientName, force);
	}

	@Override
	public String deleteClient(String clientName, DeleteClientOptions opts) throws P4JavaException {
		return clientDelegator.deleteClient(clientName, opts);
	}

	@Override
	public String switchClientView(String templateClientName, String targetClientName, SwitchClientViewOptions opts) throws P4JavaException {
		return clientDelegator.switchClientView(templateClientName, targetClientName, opts);
	}

	@Override
	public String switchStreamView(String streamPath, String targetClientName, SwitchClientViewOptions opts) throws P4JavaException {
		return clientDelegator.switchStreamView(streamPath, targetClientName, opts);
	}

	@Override
	public List<IClientSummary> getClients(final GetClientsOptions opts) throws P4JavaException {
		return clientsDelegator.getClients(opts);
	}

	@Override
	public List<IClientSummary> getClients(final String userName, final String nameFilter, final int maxResults) throws ConnectionException, RequestException, AccessException {

		return clientsDelegator.getClients(userName, nameFilter, maxResults);
	}

	@Override
	public String getCounter(final String counterName) throws ConnectionException, RequestException, AccessException {
		return counterDelegator.getCounter(counterName);
	}

	@Override
	public String getCounter(final String counterName, final CounterOptions opts) throws P4JavaException {
		return counterDelegator.getCounter(counterName, opts);
	}

	@Override
	public void setCounter(final String counterName, final String value, final boolean perforceCounter) throws ConnectionException, RequestException, AccessException {
		counterDelegator.setCounter(counterName, value, perforceCounter);
	}

	@Override
	public String setCounter(final String counterName, final String value, final CounterOptions opts) throws P4JavaException {
		return counterDelegator.setCounter(counterName, value, opts);
	}

	@Override
	public void deleteCounter(final String counterName, final boolean perforceCounter) throws ConnectionException, RequestException, AccessException {
		counterDelegator.deleteCounter(counterName, perforceCounter);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.delegator.ICountersDelegator#getCounters(com.perforce.p4java.option.server.GetCountersOptions)
	 */
	@Override
	public Map<String, String> getCounters(GetCountersOptions opts) throws P4JavaException {
		return countersDelegator.getCounters(opts);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.delegator.ICountersDelegator#getCounters(com.perforce.p4java.option.server.CounterOptions)
	 * @deprecated replaced by {@link #getCounters(com.perforce.p4java.option.server.GetCountersOptions)}
	 */
	@Deprecated
	@Override
	public Map<String, String> getCounters(CounterOptions opts) throws P4JavaException {
		return countersDelegator.getCounters(opts);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.IServer#getCounters()
	 */
	@Override
	public Map<String, String> getCounters() throws ConnectionException, RequestException, AccessException {
		return countersDelegator.getCounters();
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.impl.mapbased.server.IServerControl#init(java.lang.String, int, java.util.Properties, com.perforce.p4java.option.UsageOptions, boolean, java.lang.String)
	 */
	@Override
	public ServerStatus init(String host, int port, Properties props, UsageOptions opts, boolean secure, String rsh) throws ConfigException, ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.delegator.IDepotDelegator#createDepot(com.perforce.p4java.core.IDepot)
	 */
	@Override
	public String createDepot(@Nonnull IDepot newDepot) throws P4JavaException {
		return depotDelegator.createDepot(newDepot);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.delegator.IDepotDelegator#deleteDepot(java.lang.String)
	 */
	@Override
	public String deleteDepot(String name) throws P4JavaException {
		return depotDelegator.deleteDepot(name);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.delegator.IDepotDelegator#getDepot(java.lang.String)
	 */
	@Override
	public IDepot getDepot(String name) throws P4JavaException {
		return depotDelegator.getDepot(name);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.IServer#getDepots()
	 */
	@Override
	public List<IDepot> getDepots() throws ConnectionException, RequestException, AccessException {
		return depotsDelegator.getDepots();
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.IServer#getDepots(GetDepotsOptions)
	 */
	@Override
	public List<IDepot> getDepots(GetDepotsOptions opts) throws P4JavaException {
		return depotsDelegator.getDepots(opts);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.IServer#getRepos()
	 */
	@Override
	public List<IRepo> getRepos() throws ConnectionException, RequestException, AccessException {
		return reposDelegator.getRepos();
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.IServer#getRepos(ReposOptions)
	 */
	@Override
	public List<IRepo> getRepos(ReposOptions options) throws P4JavaException {
		return reposDelegator.getRepos(options);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.IServer#getRepos()
	 */
	@Override
	public List<IRepo> getRepos(String clientName) throws ConnectionException, RequestException, AccessException {
		return reposDelegator.getRepos(clientName);
	}

	@Override
	public List<IFileDiff> getFileDiffs(final IFileSpec file1, final IFileSpec file2, final String branchSpecName, final GetFileDiffsOptions opts) throws P4JavaException {

		return diff2Delegator.getFileDiffs(file1, file2, branchSpecName, opts);
	}

	@Override
	public InputStream getFileDiffsStream(final IFileSpec file1, final IFileSpec file2, final String branchSpecName, final GetFileDiffsOptions opts) throws P4JavaException {

		return diff2Delegator.getFileDiffsStream(file1, file2, branchSpecName, opts);
	}

	@Override
	public List<IFileDiff> getFileDiffs(final IFileSpec file1, final IFileSpec file2, final String branchSpecName, final DiffType diffType, final boolean quiet, final boolean includeNonTextDiffs, final boolean gnuDiffs) throws ConnectionException, RequestException, AccessException {

		return diff2Delegator.getFileDiffs(file1, file2, branchSpecName, diffType, quiet, includeNonTextDiffs, gnuDiffs);
	}

	@Override
	public InputStream getServerFileDiffs(final IFileSpec file1, final IFileSpec file2, final String branchSpecName, final DiffType diffType, final boolean quiet, final boolean includeNonTextDiffs, final boolean gnuDiffs) throws ConnectionException, RequestException, AccessException {

		return diff2Delegator.getServerFileDiffs(file1, file2, branchSpecName, diffType, quiet, includeNonTextDiffs, gnuDiffs);
	}

	@Override
	public List<IDiskSpace> getDiskSpace(final List<String> filesystems) throws P4JavaException {
		return diskspaceDelegator.getDiskSpace(filesystems);
	}

	@Override
	public List<IFileSpec> duplicateRevisions(final IFileSpec fromFile, final IFileSpec toFile, final DuplicateRevisionsOptions opts) throws P4JavaException {

		return duplicateDelegator.duplicateRevisions(fromFile, toFile, opts);
	}

	@Override
	public List<Map<String, Object>> getExportRecords(final ExportRecordsOptions opts) throws P4JavaException {
		return exportDelegator.getExportRecords(opts);
	}

	@Override
	public void getStreamingExportRecords(final ExportRecordsOptions opts, @Nonnull final IStreamingCallback callback, final int key) throws P4JavaException {

		exportDelegator.getStreamingExportRecords(opts, callback, key);
	}

	@Override
	public List<Map<String, Object>> getExportRecords(final boolean useJournal, final long maxRecs, final int sourceNum, final long offset, final boolean format, final String journalPrefix, final String filter) throws ConnectionException, RequestException, AccessException {

		return exportDelegator.getExportRecords(useJournal, maxRecs, sourceNum, offset, format, journalPrefix, filter);
	}

	@Override
	public List<IFileAnnotation> getFileAnnotations(final List<IFileSpec> fileSpecs, @Nonnull final DiffType wsOpts, final boolean allResults, final boolean useChangeNumbers, final boolean followBranches) throws ConnectionException, RequestException, AccessException {

		return fileAnnotateDelegator.getFileAnnotations(fileSpecs, wsOpts, allResults, useChangeNumbers, followBranches);
	}

	@Override
	public List<IFileAnnotation> getFileAnnotations(final List<IFileSpec> fileSpecs, final GetFileAnnotationsOptions opts) throws P4JavaException {

		return fileAnnotateDelegator.getFileAnnotations(fileSpecs, opts);
	}

	@Override
	public Map<IFileSpec, List<IFileRevisionData>> getRevisionHistory(final List<IFileSpec> fileSpecs, final GetRevisionHistoryOptions opts) throws P4JavaException {

		return fileLogDelegator.getRevisionHistory(fileSpecs, opts);
	}

	@Override
	public Map<IFileSpec, List<IFileRevisionData>> getRevisionHistory(final List<IFileSpec> fileSpecs, final int maxRevs, final boolean contentHistory, final boolean includeInherited, final boolean longOutput, final boolean truncatedLongOutput) throws ConnectionException, AccessException {

		return fileLogDelegator.getRevisionHistory(fileSpecs, maxRevs, contentHistory, includeInherited, longOutput, truncatedLongOutput);
	}

	@Override
	public List<IFileSpec> getDirectories(@Nonnull final List<IFileSpec> fileSpecs, final boolean clientOnly, final boolean deletedOnly, final boolean haveListOnly) throws ConnectionException, AccessException {
		return dirsDelegator.getDirectories(fileSpecs, clientOnly, deletedOnly, haveListOnly);
	}

	@Override
	public List<IFileSpec> getDirectories(final List<IFileSpec> fileSpecs, final GetDirectoriesOptions opts) throws P4JavaException {
		return dirsDelegator.getDirectories(fileSpecs, opts);
	}

	@Override
	public List<IFileSpec> getDepotFiles(@Nonnull final List<IFileSpec> fileSpecs, final boolean allRevs) throws ConnectionException, AccessException {

		return filesDelegator.getDepotFiles(fileSpecs, allRevs);
	}

	@Override
	public List<IFileSpec> getDepotFiles(@Nonnull final List<IFileSpec> fileSpecs, final GetDepotFilesOptions opts) throws P4JavaException {

		return filesDelegator.getDepotFiles(fileSpecs, opts);
	}

	@Override
	public List<IFix> fixJobs(final List<String> jobIds, final int changeListId, final String status, final boolean delete) throws ConnectionException, RequestException, AccessException {
		return fixDelegator.fixJobs(jobIds, changeListId, status, delete);
	}

	@Override
	public List<IFix> fixJobs(@Nonnull final List<String> jobIds, final int changeListId, final FixJobsOptions opts) throws P4JavaException {
		return fixDelegator.fixJobs(jobIds, changeListId, opts);
	}

	@Override
	public List<IFix> getFixList(final List<IFileSpec> fileSpecs, final int changeListId, final String jobId, final boolean includeIntegrations, final int maxFixes) throws ConnectionException, RequestException, AccessException {
		return fixesDelegator.getFixList(fileSpecs, changeListId, jobId, includeIntegrations, maxFixes);
	}

	@Override
	public List<IFix> getFixes(final List<IFileSpec> fileSpecs, final GetFixesOptions opts) throws P4JavaException {
		return fixesDelegator.getFixes(fileSpecs, opts);
	}

	@Override
	public List<IExtendedFileSpec> getExtendedFiles(final List<IFileSpec> fileSpecs, final int maxFiles, final int sinceChangelist, final int affectedByChangelist, final FileStatOutputOptions outputOptions, final FileStatAncilliaryOptions ancilliaryOptions) throws ConnectionException, AccessException {
		return fstatDelegator.getExtendedFiles(fileSpecs, maxFiles, sinceChangelist, affectedByChangelist, outputOptions, ancilliaryOptions);
	}

	@Override
	public List<IExtendedFileSpec> getExtendedFiles(final List<IFileSpec> fileSpecs, final GetExtendedFilesOptions opts) throws P4JavaException {
		return fstatDelegator.getExtendedFiles(fileSpecs, opts);
	}

	@Override
	public List<IFileLineMatch> getMatchingLines(List<IFileSpec> fileSpecs, String pattern, MatchingLinesOptions options) throws P4JavaException {
		return grepDelegator.getMatchingLines(fileSpecs, pattern, options);
	}

	@Override
	public List<IFileLineMatch> getMatchingLines(@Nonnull List<IFileSpec> fileSpecs, @Nonnull String pattern, @Nullable List<String> infoLines, MatchingLinesOptions options) throws P4JavaException {
		return grepDelegator.getMatchingLines(fileSpecs, pattern, infoLines, options);
	}

	@Override
	public IServerInfo getServerInfo() throws ConnectionException, RequestException, AccessException {
		return infoDelegator.getServerInfo();
	}

	@Override
	public String createUserGroup(IUserGroup group) throws ConnectionException, RequestException, AccessException {
		return groupDelegator.createUserGroup(group);
	}

	@Override
	public String createUserGroup(IUserGroup group, UpdateUserGroupOptions opts) throws P4JavaException {
		return groupDelegator.createUserGroup(group, opts);
	}

	@Override
	public String deleteUserGroup(IUserGroup group) throws ConnectionException, RequestException, AccessException {
		return groupDelegator.deleteUserGroup(group);
	}

	@Override
	public String deleteUserGroup(IUserGroup group, UpdateUserGroupOptions opts) throws P4JavaException {
		return groupDelegator.deleteUserGroup(group, opts);
	}

	@Override
	public IUserGroup getUserGroup(String name) throws ConnectionException, RequestException, AccessException {
		return groupDelegator.getUserGroup(name);
	}

	@Override
	public String updateUserGroup(IUserGroup group, boolean updateIfOwner) throws ConnectionException, RequestException, AccessException {
		return groupDelegator.updateUserGroup(group, updateIfOwner);
	}

	@Override
	public String updateUserGroup(IUserGroup group, UpdateUserGroupOptions opts) throws P4JavaException {
		return groupDelegator.updateUserGroup(group, opts);
	}

	@Override
	public List<IUserGroup> getUserGroups(String userOrGroupName, GetUserGroupsOptions opts) throws P4JavaException {
		return groupsDelegator.getUserGroups(userOrGroupName, opts);
	}

	@Override
	public List<IUserGroup> getUserGroups(String userOrGroupName, boolean indirect, boolean displayValues, int maxGroups) throws ConnectionException, RequestException, AccessException {
		return groupsDelegator.getUserGroups(userOrGroupName, indirect, displayValues, maxGroups);
	}

	@Override
	public List<IFileSpec> getSubmittedIntegrations(List<IFileSpec> fileSpecs, String branchSpec, boolean reverseMappings) throws ConnectionException, RequestException, AccessException {
		return integratedDelegator.getSubmittedIntegrations(fileSpecs, branchSpec, reverseMappings);
	}

	@Override
	public List<IFileSpec> getSubmittedIntegrations(List<IFileSpec> fileSpecs, GetSubmittedIntegrationsOptions opts) throws P4JavaException {
		return integratedDelegator.getSubmittedIntegrations(fileSpecs, opts);
	}

	@Override
	public IStreamIntegrationStatus getStreamIntegrationStatus(final String stream, final StreamIntegrationStatusOptions opts) throws P4JavaException {
		return statDelegator.getStreamIntegrationStatus(stream, opts);
	}

	@Override
	public IJob createJob(@Nonnull Map<String, Object> fieldMap) throws ConnectionException, RequestException, AccessException {
		return jobDelegator.createJob(fieldMap);
	}

	@Override
	public String deleteJob(String jobId) throws ConnectionException, RequestException, AccessException {
		return jobDelegator.deleteJob(jobId);
	}

	@Override
	public IJob getJob(String jobId) throws ConnectionException, RequestException, AccessException {
		return jobDelegator.getJob(jobId);
	}

	@Override
	public String updateJob(@Nonnull IJob job) throws ConnectionException, RequestException, AccessException {
		return jobDelegator.updateJob(job);
	}

	@Override
	public List<IJob> getJobs(final List<IFileSpec> fileSpecs, final int maxJobs, final boolean longDescriptions, final boolean reverseOrder, final boolean includeIntegrated, final String jobView) throws ConnectionException, RequestException, AccessException {
		return jobsDelegator.getJobs(fileSpecs, maxJobs, longDescriptions, reverseOrder, includeIntegrated, jobView);
	}

	@Override
	public List<IJob> getJobs(final List<IFileSpec> fileSpecs, final GetJobsOptions opts) throws P4JavaException {
		return jobsDelegator.getJobs(fileSpecs, opts);
	}

	@Override
	public IJobSpec getJobSpec() throws ConnectionException, RequestException, AccessException {
		return jobSpecDelegator.getJobSpec();
	}

	@Override
	public String deleteKey(final String keyName) throws P4JavaException {
		return keyDelegator.deleteKey(keyName);
	}

	@Override
	public String setKey(final String keyName, final String value, final KeyOptions opts) throws P4JavaException {

		return keyDelegator.setKey(keyName, value, opts);
	}

	@Override
	public String getKey(final String keyName) throws P4JavaException {
		return keyDelegator.getKey(keyName);
	}

	@Override
	public Map<String, String> getKeys(final GetKeysOptions opts) throws P4JavaException {
		return keysDelegator.getKeys(opts);
	}

	@Override
	public String createLabel(@Nonnull final ILabel label) throws ConnectionException, RequestException, AccessException {

		return labelDelegator.createLabel(label);
	}

	@Override
	public String deleteLabel(final String labelName, final boolean force) throws ConnectionException, RequestException, AccessException {

		return labelDelegator.deleteLabel(labelName, force);
	}

	@Override
	public String deleteLabel(final String labelName, final DeleteLabelOptions opts) throws P4JavaException {

		return labelDelegator.deleteLabel(labelName, opts);
	}

	@Override
	public ILabel getLabel(final String labelName) throws ConnectionException, RequestException, AccessException {

		return labelDelegator.getLabel(labelName);
	}

	@Override
	public String updateLabel(@Nonnull final ILabel label) throws ConnectionException, RequestException, AccessException {

		return labelDelegator.updateLabel(label);
	}

	@Override
	public List<ILabelSummary> getLabels(final String user, final int maxLabels, final String nameFilter, final List<IFileSpec> fileList) throws ConnectionException, RequestException, AccessException {

		return labelsDelegator.getLabels(user, maxLabels, nameFilter, fileList);
	}

	@Override
	public List<ILabelSummary> getLabels(final List<IFileSpec> fileList, final GetLabelsOptions opts) throws P4JavaException {

		return labelsDelegator.getLabels(fileList, opts);
	}

	@Override
	public void journalWait(final JournalWaitOptions opts) throws P4JavaException {
		journalWaitDelegator.journalWait(opts);
	}

	@Override
	public String getLoginStatus() throws P4JavaException {
		return loginDelegator.getLoginStatus();
	}

	@Override
	public void login(final String password) throws ConnectionException, RequestException, AccessException, ConfigException {
		loginDelegator.login(password);
	}

	@Override
	public void login(final String password, final boolean allHosts) throws ConnectionException, RequestException, AccessException, ConfigException {
		loginDelegator.login(password, allHosts);
	}

	@Override
	public void login(final String password, final LoginOptions opts) throws P4JavaException {
		loginDelegator.login(password, opts);
	}

	@Override
	public void login(final String password, final StringBuffer ticket, final LoginOptions opts) throws P4JavaException {
		loginDelegator.login(password, ticket, opts);
	}

	@Override
	public void login(@Nonnull final IUser user, final StringBuffer ticket, final LoginOptions opts) throws P4JavaException {
		loginDelegator.login(user, ticket, opts);
	}

	@Override
	public boolean isDontWriteTicket(final String cmd, final String[] cmdArgs) {
		return loginDelegator.isDontWriteTicket(cmd, cmdArgs);
	}

	@Override
	public List<Map<String, Object>> login2(Login2Options opts, String user) throws P4JavaException {
		return login2Delegator.login2(opts, user);
	}

	@Override
	public String getLogin2Status() throws P4JavaException {
		return login2Delegator.getLogin2Status();
	}

	@Override
	public String getLogin2Status(IUser user) throws P4JavaException {
		return login2Delegator.getLogin2Status(user);
	}

	@Override
	public Map<String, String> login2ListMethods() throws P4JavaException {
		return login2Delegator.login2ListMethods();
	}

	@Override
	public String login2InitAuth(String method) throws P4JavaException {
		return login2Delegator.login2InitAuth(method);
	}

	@Override
	public String login2CheckAuth(String auth, boolean persist) throws P4JavaException {
		return login2Delegator.login2CheckAuth(auth, persist);
	}

	@Override
	public String login2(IUser user, Login2Options opts) throws P4JavaException {
		return login2Delegator.login2(user, opts);
	}

	@Override
	public void logout() throws ConnectionException, RequestException, AccessException, ConfigException {
		logoutDelegator.logout();
	}

	@Override
	public void logout(final LoginOptions opts) throws P4JavaException {
		logoutDelegator.logout(opts);
	}

	@Override
	public List<IServerProcess> getServerProcesses() throws ConnectionException, RequestException, AccessException {

		return monitorDelegator.getServerProcesses();
	}

	@Override
	public List<IServerProcess> getServerProcesses(final GetServerProcessesOptions opts) throws P4JavaException {

		return monitorDelegator.getServerProcesses(opts);
	}

	@Override
	public ILogTail getLogTail(final LogTailOptions opts) throws P4JavaException {
		return logTailDelegator.getLogTail(opts);
	}

	@Override
	public List<IFileSpec> getOpenedFiles(final List<IFileSpec> fileSpecs, final boolean allClients, final String clientName, final int maxFiles, final int changeListId) throws ConnectionException, AccessException {
		return openedDelegator.getOpenedFiles(fileSpecs, allClients, clientName, maxFiles, changeListId);
	}

	@Override
	public List<IFileSpec> getOpenedFiles(final List<IFileSpec> fileSpecs, final OpenedFilesOptions opts) throws P4JavaException {
		return openedDelegator.getOpenedFiles(fileSpecs, opts);
	}

	@Override
	public List<IFileSpec> moveFile(final int changelistId, final boolean listOnly, final boolean noClientMove, final String fileType, @Nonnull final IFileSpec fromFile, @Nonnull final IFileSpec toFile) throws ConnectionException, RequestException, AccessException {

		return moveDelegator.moveFile(changelistId, listOnly, noClientMove, fileType, fromFile, toFile);
	}

	@Override
	public List<IFileSpec> moveFile(@Nonnull IFileSpec fromFile, @Nonnull IFileSpec toFile, @Nullable MoveFileOptions opts) throws P4JavaException {

		return moveDelegator.moveFile(fromFile, toFile, opts);
	}

	@Override
	public List<IObliterateResult> obliterateFiles(@Nonnull final List<IFileSpec> fileSpecs, final ObliterateFilesOptions opts) throws P4JavaException {

		return obliterateDelegator.obliterateFiles(fileSpecs, opts);
	}

	@Override
	public String changePassword(final String oldPassword, final String newPassword, final String userName) throws P4JavaException {
		return passwdDelegator.changePassword(oldPassword, newPassword, userName);
	}

	@Override
	public InputStream getFileContents(final List<IFileSpec> fileSpecs, final GetFileContentsOptions opts) throws P4JavaException {

		return printDelegator.getFileContents(fileSpecs, opts);
	}
	
	@Override
	public void getFileContents(ByteBuffer byteBufferContent, final List<IFileSpec> fileSpecs, final GetFileContentsOptions opts) throws Exception {

		printDelegator.getFileContents(byteBufferContent, fileSpecs, opts);
	}

	@Override
	public InputStream getFileContents(final List<IFileSpec> fileSpecs, final boolean allrevs, final boolean noHeaderLine) throws ConnectionException, RequestException, AccessException {

		return printDelegator.getFileContents(fileSpecs, allrevs, noHeaderLine);
	}

	@Override
	public String setProperty(final String name, final String value, final PropertyOptions opts) throws P4JavaException {

		return propertyDelegator.setProperty(name, value, opts);
	}

	@Override
	public List<IProperty> getProperty(final GetPropertyOptions opts) throws P4JavaException {

		return propertyDelegator.getProperty(opts);
	}

	@Override
	public String deleteProperty(final String name, final PropertyOptions opts) throws P4JavaException {

		return propertyDelegator.deleteProperty(name, opts);
	}

	@Override
	public String createProtectionEntries(@Nonnull final List<IProtectionEntry> entryList) throws P4JavaException {

		return protectDelegator.createProtectionEntries(entryList);
	}

	@Override
	public String updateProtectionEntries(@Nonnull final List<IProtectionEntry> entryList) throws P4JavaException {

		return protectDelegator.updateProtectionEntries(entryList);
	}

	@Override
	public InputStream getProtectionsTable() throws P4JavaException {
		return protectDelegator.getProtectionsTable();
	}

	@Override
	public List<IProtectionEntry> getProtectionEntries(final List<IFileSpec> fileList, final GetProtectionEntriesOptions opts) throws P4JavaException {

		return protectsDelegator.getProtectionEntries(fileList, opts);
	}

	@Override
	public List<IProtectionEntry> getProtectionEntries(final boolean allUsers, final String hostName, final String userName, final String groupName, final List<IFileSpec> fileList) throws ConnectionException, RequestException, AccessException {

		return protectsDelegator.getProtectionEntries(allUsers, hostName, userName, groupName, fileList);
	}

	@Override
	public String reload(final ReloadOptions opts) throws P4JavaException {
		return reloadDelegator.reload(opts);
	}

	@Override
	public String renameUser(final String oldUserName, final String newUserName) throws P4JavaException {

		return renameUserDelegator.renameUser(oldUserName, newUserName);
	}

	@Override
	public List<IReviewChangelist> getReviewChangelists(final GetReviewChangelistsOptions opts) throws P4JavaException {

		return reviewDelegator.getReviewChangelists(opts);
	}

	@Override
	public List<IUserSummary> getReviews(final int changelistId, final List<IFileSpec> fileSpecs) throws ConnectionException, RequestException, AccessException {

		return reviewsDelegator.getReviews(changelistId, fileSpecs);
	}

	@Override
	public List<IUserSummary> getReviews(final List<IFileSpec> fileSpecs, final GetReviewsOptions opts) throws P4JavaException {

		return reviewsDelegator.getReviews(fileSpecs, opts);
	}

	@Override
	public List<String> searchJobs(final String words, final SearchJobsOptions opts) throws P4JavaException {

		return searchDelegator.searchJobs(words, opts);
	}

	@Override
	public List<IFileSize> getFileSizes(final List<IFileSpec> fileSpecs, final GetFileSizesOptions opts) throws P4JavaException {

		return sizesDelegator.getFileSizes(fileSpecs, opts);
	}

	@Override
	public String createStream(@Nonnull final IStream stream) throws P4JavaException {
		return streamDelegator.createStream(stream);
	}

	@Override
	public IStream getStream(@Nonnull final String streamPath) throws P4JavaException {
		return streamDelegator.getStream(streamPath);
	}

	@Override
	public IStream getStream(final String streamPath, final GetStreamOptions opts) throws P4JavaException {

		return streamDelegator.getStream(streamPath, opts);
	}

	@Override
	public String updateStream(final IStream stream, final StreamOptions opts) throws P4JavaException {

		return streamDelegator.updateStream(stream, opts);
	}

	@Override
	public String deleteStream(final String streamPath, final StreamOptions opts) throws P4JavaException {

		return streamDelegator.deleteStream(streamPath, opts);
	}

	@Override
	public String convertSparseStream(final ConvertSparseOptions opts) throws P4JavaException {

		return streamDelegator.convertSparseStream(opts);
	}


	@Override
	public List<IStreamSummary> getStreams(final List<String> streamPaths, final GetStreamsOptions opts) throws P4JavaException {

		return streamsDelegator.getStreams(streamPaths, opts);
	}

	@Override
	public List<IFileSpec> tagFiles(List<IFileSpec> fileSpecs, String labelName, boolean listOnly, boolean delete) throws ConnectionException, RequestException, AccessException {

		return tagDelegator.tagFiles(fileSpecs, labelName, listOnly, delete);
	}

	@Override
	public List<IFileSpec> tagFiles(final List<IFileSpec> fileSpecs, final String labelName, final TagFilesOptions opts) throws P4JavaException {

		return tagDelegator.tagFiles(fileSpecs, labelName, opts);
	}

	@Override
	public String createTriggerEntries(@Nonnull final List<ITriggerEntry> entryList) throws P4JavaException {

		return triggersDelegator.createTriggerEntries(entryList);
	}

	@Override
	public List<ITriggerEntry> getTriggerEntries() throws P4JavaException {
		return triggersDelegator.getTriggerEntries();
	}

	@Override
	public String updateTriggerEntries(@Nonnull final List<ITriggerEntry> entryList) throws P4JavaException {

		return triggersDelegator.updateTriggerEntries(entryList);
	}

	@Override
	public InputStream getTriggersTable() throws P4JavaException {
		return triggersDelegator.getTriggersTable();
	}

	@Override
	public String createUser(@Nonnull final IUser user, final boolean force) throws ConnectionException, RequestException, AccessException {

		return userDelegator.createUser(user, force);
	}

	@Override
	public String createUser(@Nonnull final IUser user, final UpdateUserOptions opts) throws P4JavaException {

		return userDelegator.createUser(user, opts);
	}

	@Override
	public String updateUser(@Nonnull final IUser user, final UpdateUserOptions opts) throws P4JavaException {

		return userDelegator.updateUser(user, opts);
	}

	@Override
	public String updateUser(@Nonnull final IUser user, final boolean force) throws ConnectionException, RequestException, AccessException {

		return userDelegator.updateUser(user, force);
	}

	@Override
	public String deleteUser(final String userName, final boolean force) throws ConnectionException, RequestException, AccessException {

		return userDelegator.deleteUser(userName, force);
	}

	@Override
	public String deleteUser(String userName, UpdateUserOptions opts) throws P4JavaException {

		return userDelegator.deleteUser(userName, opts);
	}

	@Override
	public IUser getUser(final String userName) throws ConnectionException, RequestException, AccessException {

		return userDelegator.getUser(userName);
	}

	@Override
	public List<IUserSummary> getUsers(final List<String> userList, final int maxUsers) throws ConnectionException, RequestException, AccessException {

		return usersDelegator.getUsers(userList, maxUsers);
	}

	@Override
	public List<IUserSummary> getUsers(final List<String> userList, final GetUsersOptions opts) throws P4JavaException {

		return usersDelegator.getUsers(userList, opts);
	}

	@Override
	public String unload(final UnloadOptions opts) throws P4JavaException {
		return unloadDelegator.unload(opts);
	}

	@Override
	public List<IExtendedFileSpec> verifyFiles(final List<IFileSpec> fileSpecs, final VerifyFilesOptions opts) throws P4JavaException {

		return verifyDelegator.verifyFiles(fileSpecs, opts);
	}

	@Override
	public String renameClient(final String oldUserName, final String newUserName) throws P4JavaException {

		return renameClientDelegator.renameClient(oldUserName, newUserName);
	}

	/**
	 * Usage: ls-tree {tree-sha}
	 *
	 * @param sha graph SHA
	 * @return List of graph tree objects
	 * @throws P4JavaException API errors
	 */
	@Override
	public List<IGraphListTree> getGraphListTree(String sha) throws P4JavaException {

		return graphListTreeDelegator.getGraphListTree(sha);
	}

	/**
	 * Usage: show-ref [ -a -n {repo} -u {user} -t {type} -m {max} ]
	 *
	 * @param opts graph Show Ref Options
	 * @return list of graph refs
	 * @throws P4JavaException API errors
	 */
	@Override
	public List<IGraphRef> getGraphShowRefs(GraphShowRefOptions opts) throws P4JavaException {

		return graphShowRefDelegator.getGraphShowRefs(opts);
	}

	/**
	 * Usage: p4 spec [ -o ] type
	 *
	 * @param type spec type, ('job' or 'stream') to be updated.
	 * @return non-null result message string from the Perforce server; this may
	 * include form trigger output pre-pended and / or appended to the
	 * "normal" message.
	 * @throws P4JavaException if any error occurs in the processing of this method.
	 */
	@Override
	public Map<String, Object> getSpec(CustomSpec type) throws P4JavaException {

		return specDelegator.getSpec(type);
	}

	/**
	 * Usage: p4 spec [ -i ] type
	 *
	 * @param type spec type, ('job' or 'stream') to be updated.
	 * @param spec updated spec as a map.
	 * @return non-null result message string from the Perforce server; this may
	 * include form trigger output pre-pended and / or appended to the
	 * "normal" message.
	 * @throws P4JavaException if any error occurs in the processing of this method.
	 * @since 2020.1
	 */
	@Override
	public String updateSpec(CustomSpec type, Map<String, Object> spec) throws P4JavaException {

		return specDelegator.updateSpec(type, spec);
	}

	/**
	 * Usage: p4 spec [ -i ] type
	 *
	 * @param type spec type, ('job' or 'stream') to be updated.
	 * @param spec updated spec as a String.
	 * @return non-null result message string from the Perforce server; this may
	 * include form trigger output pre-pended and / or appended to the
	 * "normal" message.
	 * @throws P4JavaException if any error occurs in the processing of this method.
	 * @since 2020.1
	 */
	@Override
	public String updateSpecString(CustomSpec type, String spec) throws P4JavaException {

		return specDelegator.updateSpecString(type, spec);
	}

	@Override
	public List<IServerIPMACAddress> getValidServerIPMACAddress() throws P4JavaException {

		return licenseDelegator.getValidServerIPMACAddress();
	}

	@Override
	public ILicenseLimits getLimits() throws P4JavaException {

		return licenseDelegator.getLimits();
	}

	@Override
	public ILicense getLicense() throws P4JavaException {

		return licenseDelegator.getLicense();
	}

	@Override
	public String updateLicense(ILicense license) throws P4JavaException {

		return licenseDelegator.updateLicense(license);
	}


	@Override
	public String sampleExtension(String extnName) throws P4JavaException {
		return extensionDelegator.sampleExtension(extnName);
	}

	@Override
	public String packageExtension(String dirName) throws P4JavaException {
		return extensionDelegator.packageExtension(dirName);
	}

	@Override
	public ExtensionSummary installExtension(String extnPackageName, boolean allowUnsigned) throws P4JavaException {
		return extensionDelegator.installExtension(extnPackageName, allowUnsigned);
	}

	@Override
	public String createExtensionConfig(IExtension extension, String namespace, String instanceConfig) throws P4JavaException {
		return extensionDelegator.createExtensionConfig(extension, namespace, instanceConfig);
	}

	@Override
	public String updateExtensionConfig(IExtension extension, String namespace, String instanceConfig) throws P4JavaException {
		return extensionDelegator.updateExtensionConfig(extension, namespace, instanceConfig);
	}

	@Override
	public List<ExtensionSummary> listExtensions(String type) throws P4JavaException {
		return extensionDelegator.listExtensions(type);
	}

	@Override
	public String deleteExtension(String namespace, String extnName) throws P4JavaException {
		return extensionDelegator.deleteExtension(namespace, extnName);
	}

	@Override
	public Extension getExtensionConfig(String namespace, String name, String instanceName) throws P4JavaException {
		return extensionDelegator.getExtensionConfig(namespace, name, instanceName);
	}

	/**
	 * Usage: cat-file commit {object-sha}
	 *
	 * @param sha graph SHA
	 * @return Commit
	 * @throws P4JavaException API errors
	 */
	@Override
	public ICommit getCommitObject(String sha) throws P4JavaException {
		return graphCommitDelegator.getCommitObject(sha);
	}

	/**
	 * Usage: cat-file -n {repo} commit {object-sha}
	 *
	 * @param sha graph SHA
	 * @return Commit
	 * @throws P4JavaException API errors
	 */
	@Override
	public ICommit getCommitObject(String sha, String repo) throws P4JavaException {
		return graphCommitDelegator.getCommitObject(sha, repo);
	}

	/**
	 * Usage: cat-file -n {repo} blob {object-sha}
	 *
	 * @param repo graph repo
	 * @param sha  graph SHA
	 * @return InputStream for graph blob
	 * @throws P4JavaException API errors
	 */
	@Override
	public InputStream getBlobObject(String repo, String sha) throws P4JavaException {
		return graphCommitDelegator.getBlobObject(repo, sha);
	}

	/**
	 * Usage: cat-file -t {object-sha}
	 *
	 * @param sha graph SHA
	 * @return graph object
	 * @throws P4JavaException API errors
	 */
	@Override
	public IGraphObject getGraphObject(String sha) throws P4JavaException {
		return graphCommitDelegator.getGraphObject(sha);
	}

	/**
	 * @param map map
	 * @return IFileSpec
	 * @throws AccessException     on error
	 * @throws ConnectionException on error
	 * @deprecated use {@link com.perforce.p4java.impl.mapbased.server.cmd.ResultListBuilder}
	 */
	@Deprecated
	public IFileSpec handleFileReturn(Map<String, Object> map) throws AccessException, ConnectionException {
		return ResultListBuilder.handleFileReturn(map, this);
	}

	/**
	 * @param map    map
	 * @param client client
	 * @return IFileSpec
	 * @throws AccessException     on error
	 * @throws ConnectionException on error
	 * @deprecated use {@link com.perforce.p4java.impl.mapbased.server.cmd.ResultListBuilder}
	 */
	@Deprecated
	public IFileSpec handleFileReturn(Map<String, Object> map, IClient client) throws AccessException, ConnectionException {
		return ResultListBuilder.handleFileReturn(map, this);
	}

	public abstract IServerAddress getServerAddressDetails();

	@Override
	public Map<String, List<IStreamlog>> getStreamlog(List<String> streamPaths, StreamlogOptions opts) throws P4JavaException {
		return streamlogDelegator.getStreamlog(streamPaths, opts);
	}
}
