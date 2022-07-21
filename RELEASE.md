                            Release Notes for
                       P4Java, the Perforce Java API

                              Version 2021.2 Patch 5

Introduction

	P4Java is a Java-native API for accessing Perforce SCM
	services from within Java applications, servlets, plugins,
	and other Java contexts.

	Perforce numbers releases YYYY.R/CCCCC, for example, 2002.1/30547.
	YYYY is the year; R is the release of that year; CCCCC is the
	bug fix change level.  Each bug fix in these release notes is
	marked by its change number.  Any build includes (1) all bug fixes
	of all previous releases and (2) all bug fixes of the current
	release up to the bug fix change level.

	Please send all feedback to support@perforce.com

Requirements

	* Perforce server at Release 2015.1 or higher.

	* Java: full standard JDK 8 or later.  Implementation as
	  discussed in "Known Limitations" below.

	* SSL: unlimited strength JCE (Java Cryptography Extension) package for
	  256-bit encryption level SSL connection to a secure Perforce server.

SSL and Trust

	Perforce server 2015.1 or higher supports 256-bit SSL connections
	and trust establishment via accepting the fingerprint of the SSL
	certificate's public key. The standard JDK comes with 128-bit
	encryption level ciphers. In order to use P4Java to connect to
	a secure Perforce server, you must download and install the
	unlimited strength JCE package for your JDK version.

	To make a secure connection using P4Java, simply append 'ssl'
	to the end of the P4Java protocol (i.e. 'p4javassl://perforce:1667').
	For new a connection or a key change, you must also (re)establish
	trust using the IOptionsServer's 'addTrust' method. See example
	code snippet below:

	// Create a P4Java SSL connection to a secure Perforce server
	try {
	    String serverUri = "p4javassl://perforce:1667";
	    Properties props = null;
	    IOptionsServer server = ServerFactory.getOptionsServer(serverUri, props);
	    // assume a new first time connection
	    // you should also handle a key change situation...
	    server.addTrust(new TrustOptions().setAutoAccept(true));
	    // if all goes well...
	    IServerInfo serverInfo = server.getServerInfo();
	} catch (P4JavaException e) {
	    // process P4Java exception
	} catch (Exception e) {
	    // process other exception
	}

	P4Java 2020.1 has changed the default TLS support to TLSv1.2 for use 
	with Perforce server 2019.1 or greater this can	be modified using the
	JVM option:

	    java -DsecureSocketEnabledProtocols=TLSv1.2 

Documentation

	P4Java interfaces, classes, methods, etc., are documented in
	the Javadoc tree included in the P4Java distribution and by
	P4Java User Guide PDF also included in the distributed zip file.
	Advanced usage issues, example patterns, etc., are documented
	in the User Guide PDF.

Installation

	After downloading the P4Java zip distribution file, unzip the
	file's contents to a suitable directory. Copy or move the enclosed
	P4Java p4java.jar JAR file to a suitable location for use by IDEs,
	compilers, applications etc. Similarly copy or move the enclosed
	Javadoc tree to a suitable place for browsing.

Known Limitations

	* P4Java does not honor Unix / Linux umask settings on file
	  creation and sync operations by default (this is a general Java
	  JVM issue, not a P4Java issue). If this is an issue, you can supply
	  a suitable
	    'com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper'
	  interface as described  in the P4Java User Guide and associated
	  Javadoc documents.

	* P4Java is heavily reliant on the quality and performance of
	  the underlying JVM's NIO implementation; if these are in any
	  way limited or restricted on a target platform, performance
	  and even correctness may suffer. P4Java is known to work
	  correctly on the platforms listed above under "Requirements".

	* P4Java can not reliably be used in Unicode contexts where files
	  have been added to a Perforce server as "shiftjis" when they are
	  in fact CP932 (or MS932) encoded, and vice-versa. The usual
	  symptoms of this problem include being unable to sync or submit
	  affected files, with an error message that includes the string
	  "Translation of file content failed". This is a known problem due
	  to the inability to define a robust round-trip encoding / decoding
	  between these encodings and the inaccurate use of "shiftjis" to
	  mean either true shift-jis or CP932 by the Perforce server and
	  many Windows tools. There is currently no workaround known.

	* The Perforce server (2015.1 or higher) only support 256-bit
	  encryption.  Due to current US export control restrictions
	  for some countries, the standard JDK package only comes with
	  128-bit encryption level ciphers.  In order to use P4Java to
	  connect to a secure Perforce server, those living in eligible
	  countries may download the unlimited strength JCE (Java
	  Cryptography Extension) version and replace the current
	  default cryptography jar files with the unlimited strength
	  files. These files are located at:

	  <java-home>/lib/security/local_policy.jar
	  <java-home>/lib/security/US_export_policy.jar
	  
-------------------------------------------
Updates in 2021.2 Patch 5

    #2299942 (Job #108736)
        P4TRUST is no longer required for SSL connections where the server
	    provides a certificate that's not self-signed and the certificate 
	    chain can be verified by the client.  If verified, P4TRUST is 
	    not required.
	    
	    The default java truststore is used unless you specify an 
	    alternative truststore with java system properties
	    javax.net.ssl.trustStore and javax.net.ssl.trustStorePassword
	    
	    Chain Validation can be disabled using p4java property 
	    secureClientCertValidate set to 0 which does P4TRUST only.
	    Setting to 2 will skip Chain validation and will ensure
	    the server certificates' subject or subject alternate names
	    match the hostname in the server URI.  The default of 1 will 
	    validate the chain.  Both 1 and 2 fallback to P4TRUST if
	    the chain cannot be validated.
	    
	    Fingerprints will now read and write the hostname in
	    addition to the IP in the P4TRUST file.  Set the p4java property
	    secureClientTrustName to 0 to only write the IP.  The default of
	    1 writes entries for both the IP and hostname.  A value of 2
	    will only write the hostname.   A matching fingerprint for either
	    the IP or hostname will establish trust.
	    
	          
-------------------------------------------
Updates in 2021.2 Patch 4

    #2286431 (Job #099302)
        Fixed parallel sync authetication issue on case insensitive servers.
        Fixes JENKINS-48525 and JENKINS-68104.

-------------------------------------------
Updates in 2021.2 Patch 3

    #2277193 (Job #108577)
        Fixed parallel sync batchsize.

	#2277668 (Job #110201)
        Parallel sync now passes charset to parallel threads.

-------------------------------------------
Updates in 2021.2 Patch 2

    #2263992 (Job #109211)
        Fixed how view mappings are parsed in label spec.

    #2268314 (Job #108842)
        Reconcile of unchanged UTF17-LE files, no longer opens them for edit.

    #2268017 (Job #105306)
        Fixed reconcile of paths with mix of slashes.

    #2269716 (Job #110044)
        Login no longer throws exception when connected to a broker.

-------------------------------------------
Updates in 2021.2 Patch 1

    #2239495 (Job #109385)
        Supporting ticket + P4PASSWD in P4Java (ClientCrypto update with token2)

-------------------------------------------
Changes in 2021.2

    #2217804 (Job #109139)
        Update RPC API Server version to 91 (2021.2)

    #2218345 (Job #107180)
        Added support for comments in Stream spec ViewMap.

    #2215800 (Job #108303)
        Added Stream property to the Changelist spec.

    #2231817 (Job #086112)
        Fixed a bug where sending commands to an Edge server
        that would require updates to the Commit server fail
        if the commit and edge tickets are different.

    #2215800 (Job #109140)
        Added p4 streamcmds:
        p4 stream edit:
            Implemented via EditFileOptions.java,
            option editStreamSpec = "-So",
            this option will add opened Stream to changelist.

        p4 stream parentview:
            Added setStreamParentView(IStreamSummary.ParentView parentView),
            it will change the ParentView field
            and put it in the default changelist,
            ready for submit.

        p4 stream resolve:
            Implemented via ResolveFilesAutoOptions,
            option resolveStreamSpec = "-So".

        p4 stream revert:
            Implemented via RevertFilesOptions,
            options revertStreamSpec = "-So"
            and revertFileList = "-Si".

        p4 streamlog:
            New command, added Streamlog class.

    #2211045 (Job #109141)
        Added MaxOpenFiles field to the Group spec.

-------------------------------------------
Updates in 2021.1 Patch 1

    #2191638 (Job #108308)
        Added parentview field to the Stream spec.

    #2200941 (Job #108358)
        Added mergeany/mergedown options to the Stream spec.

    #2200969 (Job #108391)
        Added ExtensionSummary class to improve handling of 
        p4 extension --list command.

-------------------------------------------
Changes in 2021.1

    #2152664 (Job #107179)
        Added support for p4 undo command.

    #2152871 (Job #059080)
        Added support for p4 license command.

    #2162308 (Job #107175)
        Added support for p4 extension command.

    #2153946 (Job #107230)
        Added support for all p4 depots command arguments.

    #1979275 (CVE-2021-29425)
        Upgraded apache commons-io library to 2.11.0.

-------------------------------------------
Updates in 2020.1

    #1997582 (Job #103444)
        null check on setType method in StreamSummary.

    #1987725 #1988418 (Job #103182)
        Parse String or Integer values for HEADCHANGE and CHANGE in ExtendedFileSpec.

    #1987752 #1987754 (Job #103185)
        Add lists for OTHER_(ACTION/CHANGE/OPEN) back into ExtendedFileSpec.
    
-------------------------------------------
Changes in 2020.1

    #1986688 (Job #103147)
        Update RPC API Server version to 88 (2020.1)

    #1984234 (Job #103013)
        Update SSL to TLSv1.2.

    #1975792 (Job #101710)
        Add flag support for p4 streams -F "Name=..."

    #1982798 (Job #100914)
        Support for custom Stream specs

    #1980693 (Job #101387)
        Performance improvement for ExtendedFileSpec

    #1979275 (Job #085207)
        P4IGNORE multi-level directory support. 

    #1891536 (Job #100727) 
        Support for reconcile -t flag. job. P4JAVA-1279.

-------------------------------------------
Updates in 2019.1 

    #1938844 (Job #101882)
        Fix for ipaddr setting not being set in RPC packet.

    #1938163 (Job #101821)
        Make MapTable::Check() public in the MapAPI classes.

    #1888951 (Job #100707)
        Support temporary workspaces using client -x flag

    #1888155 (Job #100689)
        Fixed connection issue when p4charset is set to null.

    #1877890, #1879014  (Job #100476)
        Added single sign on support into P4Java.
 
    #1882700 (Job #100498)
        Support for P4CLIENTPATH - P4JAVA-1270   
            
    #1883243 (Job #100498)
        Add support for filesys.restictsymlinks

    #1883168 (Job #098414)
        CopyFilesOptions with -f flag 
        
              
-------------------------------------------
Changes in 2019.1

    #1867087, #1872532 (Job #100139)
        Fix for symlink writable issue which fixes the can't clobber writable file error.

    #1868735, #1870310 (Job #092308)
        Fix for missing BOM when syncing unicode file w/p4charset=utf8-bom.

    #1868285, #1868848, #1870310, #1871475 (Job #095794)
        Resolved symlink open for edit issue when using reconcile.

    #1826912, #1826262, #1825761, #1824484 (Job #099244)
        Fix for symlink permission issue and support for temp file rename and move

    #1822014 (Job #099164)
        Support Ditto '&' mappings in client view.

    #1821933 (Job #099149)
        Resolve symlink detection issue when using reconcile.

    #1815000 (Job #099165)
        Support reconcile streaming functionality

    #1796426 (Job #092896)
        Support Server API level 81
        
    #1722678 (Job #055900)
        Java client MapApi support with new IClient:localWhere() command.
        
    #1800490 (Job #097819)
        Use correct delegator for getShelvedFiles command
        

-------------------------------------------
Changes in 2018.1

    #1637342 (Job #094427)
        Support for second factor authentication (login2).

    #1635988 (Job #094697)
         Fix Windows reconcile edit digest check on 'UTF8' filetype.

    #1634431 (Job #084939)
        Implement file modtime check in client-side reconcile.

-------------------------------------------
Changes in 2017.2

    #1633840 (Job #094658)
        Fixed 'shiftjis' bug nonNull -> isNull.
        
    #1622239 (Job #094389)
    	Cleanup symlink files after a reconcile -w.. 

    #1600799 (Job #093734)
        Throw warning messages from execStreamCmd as RequestExceptions (e.g. p4 print)
        
    #1596014 (Job #093755)
    	﻿Allow getJob with empty name.  Used to fetch an empty job spec. 
    
    #1578376 (Job #093036)
        Update Client Spec to support 'ChangeView' and 'Backup'.

    #1577417 (Job #092995)
        Changed Property Sequence field from int to String to allow 'none' from Swarm.

    #1560334 (Job #092589)
        Add new method with repo option for getCommitObject(String sha, String repo).

    #1549994 (Job #092462)
        Add Max limits for getChangelistFiles and getShelvedFiles.

    #1533680, #1534733 (Job #092122)
	    Files of Perforce types utf16 and unicode now successfully sync.

    #1531542 (Job #092080)
    	Fixed authentication for parallel threads on edge/commit servers.

    #1528556 (Job #092030)
    	Fixed missing callback issue for parallel transmit for regular sync
	
    #1509454 (Job #084396)
    	Support for parallel sync in the Helix Versioning Engine 2017.1
    
    #1510880 (Job #091429)
    	P4Java implementation of 'p4 list'
    	
    #1510174 (Job #091430)
    	Support 'Type' field in client spec for: readonly/partitioned/graph
    	
    #1510150 (Job #090601)
    	Added Integrate Options for -Or -Ob and -2

-------------------------------------------
Changes in 2017.1

    * Support for graph depot in the Helix Versioning Engine 2017.1 
      to serve P4Jenkins 2017.1


-------------------------------------------
Changes in 2016.1

    * The JAR file no longer includes all its dependencies in a single
      large JAR file, so some third party libraries (such as Apache 
      Commons) will now be listed as external dependencies.
      
    * Parts of the API have been refactored, notably the implementation
      classes behind IServer. As part of this, the following methods
      have been deprecated:
      * The Changelist constructor for IOptionsServer
      * Server.handleFileReturn
      * FileRevisionData.getRevisionIntegrationData
      * IFileRevisionData.getRevisionIntegtrationData

-------------------------------------------
Bugs fixed in 2016.1

    #1498327 (Job #90814)
        Fix locking issue with concurrent read and write access to users 
        ticket file.

    #1433571 (Job #73675)
        Fixed issue where unable to submit file that only contains a 
        space.
    
    #1462223 (Job #81399)
        Fix +x not being synced on Linux filesystems.
    
    #1435627 (Job #84577)
        Fix for exception messages being obscured.

    #1431937 (Job #85433)
        Many fixes for UTF16 encoding issues. How P4Java handles Unicode
        text has been changed quite a bit, and there are user facing issues
        which may have required a work around before which no longer do.

    #1431937 (Job #86058)
        Fixed utf8 files being corrupted on adding carriage return.
        
    #1460161 (Job #86035, #86034)
        Fix support for files greater than 2GB in size.
        
    #1432900 (Job #86648)
        Fixed problem where UTF characters are detected as being a
        binary file rather than text file.
        
    #1464885 (Job #89313)
        Fixed handling of text files based on line endings.
        
    #1480031 (Job #89581)
        Fixed problem where results of p4 groups command was only
        being partially populated.
        
    #1479581 (Job #89596)
        Fix for when update of a job object can be ignored.

	  
-------------------------------------------
Bugs fixed in 2015.2

  #1365094 (Job #85473)
        Fix an issue when syncing files with file type 'unicode+x'.

	#1308053 (Job #83624)
	    Fixed a potential problem where busy concurrent p4 login and
	    p4 logout commands could wipe out (loose) tickets in the
	    P4TICKETFILE file by creating a shadow P4TICKETFILE lock
	    (".lck") file.

	#1305655 (Job #80437)
	    Fixed an issue where large files (size > 2GB) are being
	    truncated when synced on the Windows plaform.

	#1305257 (Job #80413)
	    Fixed an issue when syncing files with size > 2GB (number of
	    bytes > java.lang.Integer.MAX_VALUE).

	#1244354 (Job #81080)
	    Fixed an issue with P4Java connecting and authenticating to
	    a Perforce server with (man-in-middle) security configurable
	    'net.mimcheck' set to value >= 4.

	#1237901 (Job #80389)
	    Fixed a file corruption issue with P4Java shelving/unshelving
	    of utf16-le files in the Windows environment.

	#1244455 (Job #70733)
	    Fixed a potential data loss problem with P4Java's handling of
	    protections. P4Java now uses the innate ordering of Java List
	    instead of explicitly setting the order of each protection entries.

	#1234279 (Job #67229)
	    Fixed an issue where the 'integrate' command fails if the current
	    client set on P4Java is not "myclient" when executing myclient's
	    integrateFiles() methhod.

	#1222114 (Job #62825)
	    Fixed an issue where the 'shelve' command fails if the current
	    client set on P4Java is not "myclient" when executing myclient's
	    shelveChangelist() methhod.

Minor new functionality in 2015.2

	#1310414 (Job #83909)
	    Add support for "SpecMap" in the depot spec. For spec depots,
	    the optional description of which specs should be saved, as
	    one or more patterns. For example, "-//spec/client/*_tmp*".

	#1310411 (Job #83908)
	    Add support for "tangent" depot type. It defines a read-only
	    location which holds tangents created by the 'fetch -t' command.
	    The tangent depot named 'tangent' is automatically created by
	    'fetch -t' if one does not already exist.

	#1310141 (Job #83907)
	    Added support for the "StreamDepth" field in the depot spec.
	    For stream depots, the optional depth to be used for stream
	    paths in the depot, where depth equates to the number of
	    slashes following the depot name of a stream. This field is
	    referenced when streams are being created. The default is '1',
	    matching the traditional stream name. This value may not be
	    updated once streams or archive data exist within the depot.

	#1243317 (Job #80744)
	    Added support for stream path type 'import+'. It is the same
	    as 'import' except that files can be submitted to the import
	    path. This feature requires Perforce server version >= 2014.2.

-------------------------------------------
Bugs fixed in 2015.1

	#1208529 (Job #79753)
	    Updated the P4Java 2015.1 default client API level to 78,
	    representing the "2015.1 capabilities".

	#1206466 (Job #79502)
	    Fixed an issue with child process I/O streams not properly
	    closing when RPC socket pool is enabled (pool size > 0) and
	    P4Java is starting new 'RSH' mode Perforce server processes.

	#1207366 (Job #77850)
	    Fixed an issue with 'utf16+x' files synced as 'UTF-8' with
	    UNIX line endings.

	#1206436 (Job #74785)
	    Fixed an issue where utf16 little endian files synced as big
	    endian in a little endian client platform. When utf16 files
	    are written to a client, they are written with a BOM in client
	    byte order. (See 'p4 help filetypes').

	#1059603 (Job #78811)
	    Fixed issue where Perforce depot archive file content for a
	    symlink was missing a '\n' when file type is 'symlink+F' when
	    using P4Java for submit.

	#1054452 (Job #78085)
	    Changed 'noclient' to '____CLIENT_UNSET____' as default unset
	    client name.


	#981766 (Job #74948, 75630)
	    Fixed an issue where the execution bits are not set correctly
	    when syncing files. The 'p4' client sets the Owner/Group/World
	    bits.

Major new functionality in 2015.1

	#1001469 (Job #34706)
	    Added support for starting a 'RSH' mode Perforce server from
	    P4Java. This feature is known as the "Perforce RSH hack". P4Java
	    URI example: "p4jrsh:///path/to/p4d -r /tmp/p4droot -i --java".
	    This feature requires Perforce server version 2015.1 or above.

	#980238 (Job #76501, 76540)
	    Added support for replacement fingerprint in an SSL connection.
	    If a replacement fingerprint exists for a connection and the
	    primary fingerprint does not match while the replacement
	    fingerprint does, the replacement fingerprint will replace
	    the primary. This corresponds to the 'p4 trust -r' option.

Minor new functionality in 2015.1

	#1036870 (Job #78145)
	    Implemented P4Java handling of 'login' and passed-in auth ticket
	    in a Perforce cluster environment (i.e. "serverAddress = P4Cluster").

	#1020252 (Job #77547)
	    Added the 'checkModTime' option to ReconcileFilesOptions;
	    used in conjunction with -e can be used to minimize costly
	    digest computation on the client by checking file modification
	    times before checking digests to determine if files have been
	    modified outside of Perforce. This option correspond to the
        '-m' flag in the 'p4 reconcile' command.

	#1013627 (Job #77517, 77476)
	    Added the 'updateWorkspace' option to ReconcileFilesOptions;
	    forces the workspace files to be updated to match the depot.
	    This option correspond to the '-w' flag in the 'p4 reconcile'
	    command.

-------------------------------------------
Bugs fixed in 2014.1

	#940015, 958821 (Job #74971)
	    Fixed an issue where Label.isLocked() always returns false.
	    This method pertains to the "locked/unlocked" label update
	    option. Also, added a new method Label.isAutoReload() for
	    the "autoreload/noautoreload" option.

	#907023 (Job #74183)
	    Fixed a problem where P4Java returns "file does not exist"
	    messages when syncing deleted files. The solution is to bypass
	    the delete operation if the file doesn't exist.

	#873061 (Job #73266)
	    Fixed an issue where P4Java does not capture output in local
	    file syntax when running the 'p4 reconcile -l' command option.
	    This option is set via the ReconcileFilesOptions.setLocalSyntax()
	    method with 'true' as the input parameter value.

	#872927 (Job #69493)
	    Fixed an issue where the p4trust file cannot be set using the
	    "trustPath" or the long form "com.perforce.p4java.trustPath"
	    property when requesting an IOptionServer from the ServerFactory.

	#854818 (Job #70533)
	    Fixed a regression issue where the IFileSpec.getOriginalPath()
	    returning null. Now, it will try to return the pseudo-type
	    PathType.ORIGINAL path, if it is not null. Otherwise, it will
	    return the PathType.CLIENT path.

	#849483 (Job #71635)
	    Fixed an issue where malformed exclude protection entries
	    occurring when the depot paths with whitespace are quoted.
	    The IOptionsServer.updateProtectionEntries() method corresponds
	    to the 'p4 protect -i' command.

	#842721 (Job #71340)
	    Fixed an issue where unchanged large size files with Unicode
	    chars showing as different on Windows using Perforce client
	    charset 'utf8'. The IClient.getDiffFiles() method corresponds
	    to the 'p4 diff -s<flag>' command.

	#841016 (Job #72717)
	    Fixed a permissions issue with the Perforce tickets file in
	    the Windows environment.

	#840491 (Job #72688)
	    Fixed a problem with the IClient.reconcileFiles() method
	    throwing an exception when file path contains whitespace.
	    The MapEntry class requires double quotes to placed around
	    file path with whitespace. This method corresponds to the
	    'p4 reconcile' command.

	#839020 (Job #72521)
	    Fixed an issue with the Server.getFileContents() method
	    returning null InputStream for non-existing file or revision.
	    This method corresponds to the 'p4 print' command.

Major new functionality in 2014.1

	#948217 (Job #71639)
	    Added a new P4Java API method 'renameUser()' to
	    IOptionsServer for completely renaming a user, modifying
	    all database records which mention the user. This method
	    corresponds to the 'p4 renameuser' command. This command
	    requires 'super' access.

	#876221 (Job #72273)
	    Added support for automatically detect and setup P4Java's
	    Perforce client charset when connected to an Unicode enabled
	    Perforce server. The automatic client charset is provided by
	    the JVM and OS platform. If Perforce doesn't support the
	    detected client charset it will default to 'utf8'.

	#851606 (Job #72689)
	    Added a new P4Java API method 'getShelvedFiles()' to
	    IOptionsServer for retrieving a list of shelved files
	    from a pending change list. This method corresponds to
	    the 'p4 describe -s -S <changelist>' command.

	#848677 (Job #71702, 72794)
	    Added new P4Java API methods 'getTriggerEntries()',
	    'createTriggerEntries()' and 'updateTriggerEntries()'
	    to IOptonsServer for retrieving and updating the Perforce
	    triggers table. The methods correspond to the 'p4 triggers'
	    command. This command requires 'super' access.

Minor new functionality in 2014.1

	#949065 (Job #70485)
	    Added the 'wipeAddFiles' option to RevertFilesOptions;
	    causes files that are open for add to be deleted from the
	    workspace when they are reverted. This option correspond to
	    the '-w' flag in the 'p4 revert' command.

	#944457 (Job #70486)
	    Added the 'outputDifferFilesOnly' option to GetFileDiffsOptions;
	    limits output to files that differ. This option correspond to
	    the '-Od' flag in the 'p4 diff2' command.

	#895748 (Job #71638)
	    Added support for Greek character sets 'cp1253' (Windows
	    Greek), 'cp737' (PC Greek), and 'iso8859-7' (Latin/Greek
	    Alphabet).

	#873818 (Job #59516)
	    Added a new method 'getServerCalendar()' to IServerInfo
	    for retrieving the Perforce server date as a Calendar
	    object, with the server timezone related information.
	    For examples, '2014/06/05 10:28:16 -0700 PDT' and
	    '2014/06/05 17:10:53 -0700 Pacific Daylight Time'.

	#852880 (Job #72336)
	    Included the submitted 'time' in the FileSpec, accessible
	    via the IFileSpec.getDate() method. The 'time' for each
	    file is returned from the Perforce server when listing
	    depot files using the IOptionsServer.getDepotFiles() method.
	    This method corresponds to the 'p4 files' command.

-------------------------------------------
Major new functionality in 2013.2

	#674297 (Job #62361)
	    Added a new P4Java API method 'getProtectionsTable()' to
	    IOptionsServer for retrieving the Perforce protections table.
	    This corresponds to the 'p4 protect -o' command option.
	    Note that after protections are defined, the 'p4 protect'
	    command requires 'super' access.

	#677643 (Job #66386)
	    Added a new P4Java API method 'submitShelvedChangelist()'
	    to IClient for allowing the submit of a shelved changelist
	    without transferring files or modifying the workspace.
	    This corresponds to the 'p4 submit -e shelvedChange#'
	    command option. Note that the shelved changelist must be
	    owned by the person submitting the change, but the client
	    may be different. No files may be open in any workspace
	    at the same changeless number.

	#689881 (Job #67365)
	    Added a new P4Java API method 'getFileSizes()' to the
	    IOptionsServer interface. This method will return a list
	    of file sizes for one or more files in the depot. This
	    corresponds to the 'p4 sizes' command.

	#709742 (Job #68493)
	    Added a new P4Java API method 'journalWait()' to the
	    IOptionsServer interface. This method turns on/off the
	    journal-wait. The client application can specify "noWait"
	    (off) replication when using a forwarding replica or an
	    edge server. This corresponds to the deep undoc
	    'p4 journalwait [-i]' command.

Minor new functionality in 2013.2

	#642703 (Job #66150)
	    Added support for the 'IsUnloaded = 1' tagged output for
	    the 'streams -U', 'clients -U' and 'labels -U' commands by
	    implementing the isUnloaded() method on the IStreamSummary,
	    IClientSummary and ILabelSummary interfaces.

	#656821 (Job #66779)
	    Upgraded the JZlib compression library from version 1.1.1 to
	    version 1.1.2. A list of changes can be viewed from the JZlib
	    project website: http://www.jcraft.com/jzlib/ChangeLog

	#659272 (Job #66882)
	    Added a method in the SymbolicLinkHelper class to get the last
	    modified time of symlinks with non-existing targets. Note that
	    it only works with symlink capable Java versions and systems.

	#662206 (Job #62832)
	    Added the 'shortOutput' option to OpenedFilesOptions; produces
	    optimized output when used with the -a (all clients) option.
	    This option corresponds to the '-s' flag in the 'p4 opened'
	    command.

	#671504 (Job #59611)
	    Added support in ExtendedFileSpec for capturing propagating
	    attributes and attribute types from the 'fstat -Oa' command
	    output.

	#672814 (Job #59617)
	    Added the 'groupName', 'userName' and 'ownerName' options to
	    GetUserGroupsOptions; indicating that the 'name' argument is
	    a(n) group/user/owner. These options correspond to the '-g',
	    '-u' and '-o' flags in the 'p4 groups' command.

	#673539 (Job #59624)
	    Added the 'suppressHeader' and 'showBinaryContent' options
	    to GetFileAnnotationsOptions; suppresses the one-line header
	    and displays the content of binary files. These options
	    correspond to the '-q' and '-t' flags in the 'p4 annotate'
	    command.

Bugs fixed in 2013.2

	#648450 (Job #66455)
	    Fixed an issue with P4Java not being able to add symlinks
	    due to the missing 'xfiles' RPC protocol information. This
	    occurs when the user is using a RPC socket connections pool.

	#649176 (Job #66501)
	    Fixed an issue with P4Java not being able to add symlinks
	    pointing to directories. P4Java mistakenly detecting these
	    symlinks as real directories.

	#657580 (Job #66811)
	    Fixed an issue with P4Java not being able to add/submit
	    symlinks with non-existing targets.

	#666385 (Job #67072)
	    Fixed an issue with P4Java using the wrong last modified
	    time when adding a symlink that points to a directory. It
	    was wrongly using the symlink target's last modified time.

	#670576 (Job #67305)
	    Properly allowed P4Java's RPC sockets to be configured with
	    user defined properties first before connecting to the server.

	#676527 (Job #67386)
	    Updated P4Java to use a lowercase username to save and
	    retrieve the auth ticket when connected to a case insensitive
	    Perforce server.

	#678044 (Job #67571)
	    Fixed an issue with P4Java when running 'resolve -at' of a
	    symlink that points to a null reference (non-existing target).

	#693440 (Job #68151)
	    Fixed an issue where FileAction.UNKNOWN.toString() throws a
	    P4JavaError (name / ordinal mismatch).

	#696011 (Job #68281)
	    Fixed an issue with P4Java when syncing a text file revision
	    with the current local head revision as a symlink pointing
	    to a non-existing target.

	#709753 (Job #68751)
	    Fixed an issue with P4Java where binary files are corrupted
	    in the Windows environment when sync is forced and the files
	    are already in the have list.

	#717231 (Job #68974)
	    Fixed an issue with the 'diff -sa' command (IClient.getDiffFiles())
	    returning results (filespecs), even though the content of the files
	    were not modified. This happens when using a client workspace with
	    the "LineEnd: win" spec running on the Mac/Linux/UNIX platforms.

	#748489 (Job #69733)
	    Fixed an issue with the IClient.getDiffFiles() method returning
	    wrong diff results when operating on 'utf8-bom' encoded files
	    on the Windows platforms.

-------------------------------------------
Major new functionality in 2013.1

	#546744 (Job #61136)
	    Added support for unpacking the AppleSingle file format.
	    This corresponds to the Perforce 'apple' file type. When
	    syncing 'apple' type files, P4Java will extract the data
	    fork and resource fork into two separate files. The data
	    file will retain the original file name, and the resource
	    file will have a '%' sign prefix. Note that the user must
	    stitch the data fork and resource fork back to an AppleSingle
	    file before submitting changes back to the Perforce server.

	#548578 (Job #61060, 61048)
	    Added support for connecting to an IPv6 server address URI.
	    IPv6 server addresses need to be encapsulated within square
	    brackets (i.e. 'p4java://[fe80:0:0:0:0:0:c0a8:108]:1666'),
	    follow by a ':' and a port number. Auth tickets and trusted
	    fingerprints also support the IPv6 server address format.

	#557503 (Job #59734)
	    Added the ability to cancel a long running command in P4Java
	    (i.e. syncing many files or a large file). The command can
	    be cancelled by returning false in the IStreamingCallback's
	    handleResult(...) method.

	#572509 (Job #61531)
	    Added support for "spec -o" commands ('client -o', etc.).
	    This feature can only be used with the raw "exec" methods
	    (execMapCmd(), execStreamCmd(), etc.) of the IServer and
	    IOptionsServer interfaces. The user must pass to those
	    raw methods an 'inMap' as parameter with an 'useTags' map
	    entry key and its value set to 'no'. The returning result
	    will be the non-tagged output of the requested spec.

	#602176 (Job #64162)
	    Added new methods IOptionsServer.get/setTicketsFilePath(...)
	    and IOptionsServer.get/setTrustFilePath(...) for getting
	    and setting the auth tickets file and the trust file paths.

	#621534 (Job #65076)
	    Added new P4Java API methods to use the list data structure
	    to store the result maps instead of array. Refactored all
	    affected P4Java code to use these new methods: execMapCmdList,
	    execInputStringMapCmdList and execQuietMapCmdList.

	#629318 (Job #65303)
	    Added a new low-level IFilterCallback interface that allows
	    developers to decide which key/value pairs should be skipped
	    from going into the result maps. Currently, the IFilterCallback
	    interface is supported as a parameter in the following P4Java
	    public API methods: IOptionsServer.execMapCmdList(�) and
	    IOptionsServer.execInputStringMapCmdList(�).

	#639161 (Job #59605)
	    Implemented the 'p4 property' command. Add, delete, or list
	    property values in the Perforce server. The Perforce server
	    does not directly use property values. It provides storage
	    of property values for use by applications that wish to
	    persistently store their configuration settings and other
	    property data in the Perforce server.

	#649812 (Job #66524)
	    Added proper handling of P4Java's Perforce extended charsets
	    if they are not loaded in the VM's bootstrap classpath. For
	    example, the P4Java JAR file is inside a WAR deployed in a
	    web app container like Jetty, Tomcat, etc.

Minor new functionality in 2013.1

	#529831 (Job #59317, 38737)
	    Added the ability to set socket performance preferences by
	    passing a property to the ServerFactory when requesting an
	    IOptionsServer object. The property key can be either the
	    short form "sockPerfPrefs" or the long form
	    "com.perforce.p4java.rpc.sockPerfPrefs". The property value
	    should be exactly three integers (i.e. "1, 2, 0") as a string,
	    separated by commas. The default is set to the relative importance
	    of short connection time, low latency, and high bandwidth.

	#528658 (Job #59081, 38737)
	    Upgraded the JZlib compression library from version 1.0.7 to
	    version 1.1.1. A list of changes can be viewed from the JZlib
	    project website: http://www.jcraft.com/jzlib/ChangeLog

	#528663 (Job #59114, 38737)
	    Updated P4Java to use JZlib's pure Java implementation of the
	    CRC32 checksum. This avoids the basic Java CRC32's JNI overhead
	    for certain uses of checksumming where many small pieces of data
	    are checksummed in succession.

	#529232 (Job #59116, 38737)
	    Updated P4Java to use BufferedOutputStream to improve file
	    writing performance during the 'sync' command.

	#529227 (Job #59115, 38737)
	    Updated P4Java to use BufferedInputStream to improve RPC socket
	    stream reading performance.

	#559385 (Job #59637)
	    Added the 'quiet' option to CopyFilesOptions, IntegrateFilesOptions
	    and MergeFilesOptions; corresponds to the "-q" flag in 'p4 copy',
	    'p4 integrate' and 'p4 merge' commands. If true, suppresses the
	    normal informational file list messages from being sent.

	#565178 (Job #59638)
	    Added the ability to set the 'quiet' mode "p4 -q <command>" RPC
	    protocol option to suppress all info-level output client messages.
	    To enable protocol 'quite' mode set the "quietMode" or the long
	    form "com.perforce.p4java.quietMode" property to "true" (anything)
	    when requesting an IOptionServer from the ServerFactory.

	#565975 (Job #59627)
	    Added support for the 'resolveType' field of the 'p4 fstat -Or'
	    command's output.

	#566909 (Job #59625, 59632)
	    Added support for the 'baseParent' field of the 'p4 streams'
	    command's tagged or/and filtered output.

	#581288 (Job #62715)
	    Added the 'nameFilters' option to GetCountersOptions and GetKeysOptions;
	    corresponding to the '-e[nameFilter] -e[nameFilter] -e[nameFilter] ...'
	    flags in the 'p4 counters' and 'p4 keys' commands. This option handles
	    multiple nameFilter flags as arguments to the aforementioned commands.

	#591416 (Job #62697)
	    Added the 'force' option to ChangelistOptions; corresponds to
	    the "-f" flag in 'p4 change' command. If true, it can force display
	    of the 'Description' field in a restricted changelist using the
	    IOptionsServer.getChangelist(int id, ChangelistOptions opts) method.

	#596795 (Job #63310)
	    Added support for force delete of another user's pending changelist
	    using the IOptionsServer.getChangelist(int id, ChangelistOptions opts)
	    method with the 'force' option set to true in ChangelistOptions. This
	    option corresponds to the "-f" flag in 'p4 change' command.

	#639392 (Job #66055)
	    Added the 'stream' option to UnloadOptions and ReloadOptions;
	    corresponds to the "-s stream" flag in 'p4 unload' and 'p4 reload'
	    commands. If set, it unloads/reloads the specified task stream.
	    Also, added the 'locked' option to UnloadOptions; corresponds to
	    the "-L" flag in the 'p4 unload' command. If true, it specifies
	    that the client, label, or task stream should be unloaded even if
	    it is locked.

Bugs fixed in 2013.1

	#544735 (Job #60527)
	    Fixed an issue with high-ascii client names (i.e. umlaut '�')
	    when running the 'where' command against a non-unicode server.

	#543339 (Job #60376)
	    Added handling of the 'replaced' file action. This file action
	    occurs when replacing a file with the same name, that was mapped
	    from a different source.

	#560914 (Job #61935)
	    Fixed an issue with text file (high-ascii chars in name or path)
	    identified as a symlink during an add operation.

	#551070 (Job #60128)
	    Deprecated ZeroConf support in P4Java. The server and proxy no
	    longer support the Zeroconf server registration protocol. This
	    takes effect on Perforce server version 2013.1 and above.

	#615005 (Job #64015)
	    Fixed a problem with user auth tickets sharing a singleton storage
	    when the same user is used in multiple RPC connections logging in
	    and out frequently.

	#627400 (Job #65464)
	    Fixed a possible memory leak by removing the File.deleteOnExit()
	    call in P4Java. Java accumulates the file path information in
	    memory every time the File.deleteOnExit() call is made. If the
	    the Java program stays alive for a long period of time, the
	    memory usage will grow.

	#627608 (Job #65305)
	    Fixed an issue with the AuthTicketsHelper throwing an exception
	    (FileNotFoundException) when trying to retrieve auth tickets
	    from an auth tickets file that does not exist.

	#633427 (Job #65872)
	    Fixed a problem with P4Java not properly handling (save/change)
	    passwords with high-ascii/unicode chars (i.e. umlaut '�').

	#655384 (Job #66260)
	    Fixed an issue with P4Java's Perforce file type detection
	    (infer file type by content) mistakenly detected 'utf16'
	    (unicode) files as binary.

	#645046 (Job #66294)
	    Fixed an issue with P4Java not properly converting the 'diff2'
	    command header output to the client's charset.

	#670224 (Job #67176)
	    Fixed an issue with P4Java's login method returning the wrong
	    auth ticket requested for a specified host IP address. This
	    corresponds to the 'p4 login -h<host>' command option.

-------------------------------------------
Major new functionality in 2012.3

	#487658 (Job #38013, 34097, 59186)
	    Added support for symbolic links with JDK 7 running on UNIX/Linux
	    (UNIX-like) operating systems and newer versions of Microsoft
	    Windows (Windows Vista or above). Microsoft has hard links
	    support as of Windows 2000. However, UNIX-like symbolic links
	    are only supported as of Windows Vista.

	#494418 (Job #57603)
	    Added support for progress indicators (p4 -I <command>). This
	    feature is supported by Perforce server 2012.2 or higher. To
	    enable 'p4 -I' set the "com.perforce.p4java.enableProgress"
	    or "enableProgress" property to "true" (or anything) when
	    requesting an IOptionServer from the ServerFactory class (as
	    the Properties 'props' parameter pass to the getOptionsServer()
	    method). This feature makes sense to be used with the "raw"
	    IServer.execStreamingMapCommand() method. This streaming method
	    takes a callback handler and continuously report progress during
	    the execution of a command. Note that currently only some commands
	    return progress indicators.

	#495802 (Job #58105, 56531)
	    Added support for unload and reload clients and labels. Implemented
	    the 'p4 unload' and 'p4 reload' commands. Added the '-U' option for
	    'p4 clients' and 'p4 labels'. By default, users can only unload and
	    reload their own clients or labels.  The '-f' flag requires 'admin'
	    access, which is granted by 'p4 protect'.

	#502033 (Job #40480)
	    Added the IOptionsServer.getStreamingExportRecords(...) method.
	    This method returns each exported journal or checkpoint
	    record as it comes in from the Perforce server using the
	    IStreamingCallback handleResult method.

	#505864 (Job #58523)
	    Added support for the 'p4 populate' command as the
	    populateFiles(...) method on IOptionsServer. This method
	    branches a set of files (the 'source') into another depot
	    location (the 'target') in a single step.  The new files
	    are created immediately, without requiring a 'p4 submit'
	    or a client workspace.

	#519075 (Job #59388)
	    Added new IClient.sync(...) and IChangelist.submit(...) methods
	    with IStreamingCallback. When progress indicators are turned on
	    (set the "com.perforce.p4java.enableProgress" or "enableProgress"
	    property to "true" when requesting an IOptionServer from
	    the ServerFactory), the results are sent to the user via
	    IStreamingCallback.handleResult(...), as each comes in from
	    the server.

	#521298 (Job #59434)
	    Added support for streaming back incremental progress details
	    of syncing and submitting files. The file data chunks info
	    (bytes written so far...) will be sent back to the user via
	    IStreamingCallback.handleResult(...).  This feature requires
	    the progress indicators flag (p4 -I) to be on, set the
	    "enableProgress" or "com.perforce.p4java.enableProgress"
	    property to "true" when you request an IOptionServer from
	    the ServerFactory.

	#525729 (Job #59814)
	    Added in-memory storage for auth tickets and fingerprints.
	    By default, P4Java uses files to store auth tickets and SSL
	    connection fingerprints. To turn on in-memory storage of auth
	    tickets and fingerprints set the "useAuthMemoryStore" or the
	    long form "com.perforce.p4java.useAuthMemoryStore" property
	    to "true" (or anything) when requesting an IOptionServer from
	    the ServerFactory.

Minor new functionality in 2012.3

	#488202 (Job #50343)
	    Automatically set the user's auth ticket (if available) when
	    calling the Server.setUserName(String userName) method. Unset
	    the user's auth ticket after logging out. This is useful when
	    you are using (switching between) multiple Perforce users
	    within the same instance of P4Java.

	#499857 (Job #50047)
	    Added the 'dontAnnotateFiles' option to the GetFileContentsOptions
	    class.  If true, don't append revision specifiers (# and @) to
	    the filespecs (Parameters.processParameters(...)). By default
	    the filespecs passed to IOptionsServer.getFileContents(...)
	    would get revisions appended to them during parameter processing.
	    Note that this is not a standard option for the 'p4 print' command.

Bugs fixed in 2012.3

	#491077 (Job #57711)
	    Fixed an issue with 'p4ticketsxxxx.txt' files accumulating in
	    temporary directory (Java system property "java.io.tmpdir").
	    Delete the temp tickets file immediately after usage. If the
	    rename (move) operation is successful there is no need to
	    delete the temp tickets file (it is already moved to the
	    target file). If it is copied to the target file P4Java
	    will delete it after the copy operation. If the immediate
	    deletion fails P4Java will call deleteOnExit() as a fallback.
	    Note that if the temp tickets file is locked (for some reason)
	    P4Java will not be able to delete it.

	#491101 (Job #57797)
	    Fixed a possible issue with fingerprints files accumulating in
	    temporary directory (Java system property "java.io.tmpdir").
	    Delete the temp fingerprints trust file immediately after
	    usage, and call deleteOnExit() as a fallback.

	#497015 (Job #55040)
	    Added handling of the 'types' status returning from a 'diff2'
	    command.  This fixes the problem with Server.getFileDiffs()
	    returning an IFileDiffs record with a status of "null" when
	    two identical files (revisions) differ only by filetype.

	#497448 (Job #49896)
	    Fixed an issue with the Label.updateOnServer() method raises
	    exception with message "label not associated with any Perforce
	    server", even though the user had supplied one via the
	    Label.newLabel(...) method. Attaching the IServer to the
	    newly created Label inside the Label.newLabel(...) method
	    resolved this problem.

	#516088 (Job #59253)
	    Fixed an issue with parsing exotic client view mappings
	    (i.e. file paths with double quotes within, etc.).

	#520350 (Job #59485)
	    Fixed the handling of passwords containing valid whitespace
	    characters (i.e. spaces or tabs) at the beginning or end.

	#524943 (Job #59813)
	    Fixed concurrency issues with multithreaded P4Java access
	    to the Perforce tickets and fingerprints files. This fix
	    only applies to the same instance of the JVM.

	#531260 (Job #59311)
	    Fixed an intermittent ConnectionException issue with P4Java
	    talking to a Perforce broker using an instance of the NTS
	    (non-thread-safe) version of the P4Java RPC implementation.

	#537649 (Job #36721)
	    Fixed an issue with syncing Japanese files containing special
	    double-byte characters. Updated P4Java to use the Microsoft
	    code page 932 charset implementation (superset of shiftjis).

	#538849 (Job #60524, 59311)
	    Fixed a P4Java RPC communication timeout issue when talking
	    to a P4Broker sitting between P4Java and a Perforce server.

	#549353 (Job #61402, 60798)
	    Added support for the protocol level "app" tag. The user can
	    set the value for the 'app' protocol tag by passing a property
	    to the ServerFactory when requesting an IOptionsServer object.
	    The property key can be either the short form "applicationName"
	    or the long form "com.perforce.p4java.rpc.applicationName".

	#575950 (Job #62513)
	    Updated P4Java to handle multiple (sometimes different) auth
	    tickets while connected to a replica. Note there are limitations
	    using a replica server versus the master server directly. Please
	    see the "Perforce Replication" documentation for more details.

	#616470 (Job #64875)
	    Fixed a regression bug where P4Java is not automatically loading
	    the user's auth ticket from the tickets file.

	#621705 (Job #65168)
	    Fixed an issue with P4Java not updating the password correctly
	    when it is preceded with a deletion of the current password.

-------------------------------------------
Major new functionality in 2012.2

	#467995 (Job #54780)
	    Implemented the 'p4 reconcile' command. Open files for add,
	    delete, and/or edit to reconcile client with workspace changes
	    made outside of Perforce (Perforce server 2012.1 or higher).

	#483886 (Job #57185)
	    Implemented the 'p4 duplicate' command. Duplicate revisions with
	    integration history (unsupported). This command requires 'admin'
	    access granted by 'p4 protect'.

	#470782 (Job #43626)
	    Implemented the 'p4 review' command. Lists changelists that
	    have not been reviewed before, as tracked by the specified
	    counter.

Minor new functionality in 2012.2

	#469441 (Job #55297)
	    Added conditional checking for password not set for the user
	    during login. If the passed-in password is not null/empty and
	    the server return message indicates password not set (login not
	    required) for the user, then throw an AccessException.

	#474051 (Job #51848)
	    Added a new IOptionsServer.getServerProcesses method that takes
	    a GetServerProcessesOptions parameter. This method gives more
	    information about Perforce server processes for monitoring.

	#473880 (Job #55600)
	    Added the skipFile option to ResolveFilesAutoOptions; corresponds
	    to the '-s' option in p4 resolve. This option indicates to 'skip
	    this file' during resolve.

	#473721 (Job #44095)
	    Passed more info to the IProgressCallback.tick() method. Added
	    support for sync (similar to the 'p4 sync' standard output).
	    Also, included the 'totalFileSize' and 'totalFileCount' info.

-------------------------------------------
Major new functionality in 2012.1

	#401481 (Job #36273, 51219)
	    Implemented the 'p4 passwd' command with support for password
	    change on security level 2 or 3 Perforce servers. Added support
	    for passwords longer than 16 characters; cease truncating
	    passwords to 16 bytes (Perforce server 2011.1 or higher)

	#403123 (Job #51534)
	    Added SSL support for encrypted secure connections to SSL
	    enabled Perforce servers. The only change to make a secure
	    connection using P4Java is to append 'ssl' at the end of the
	    P4Java protocol (i.e. 'p4javassl://perforce:1667').
	    * Check the "Known Limitations" for possible SSL issues.

	#428335 (Job #53040)
	    Added support for 'p4 trust' command and public key fingerprint
	    checking for SSL connections to a secure Perforce server. For a
	    new connection or mismatched key the user must run the 'addTrust'
	    methods to establish trust (again).

	#410387 (Job #51892)
	    Added the ability to ignore files for the 'p4 add' command.
	    For each 'add' file, P4Java checks the patterns defined in
	    the 'P4IGNORE' files along the 'add' file's path to determine
	    whether the 'add' file should be ignored or not.

	#416539 (Job #52418, 40151, 39722)
	    Updated the NTS (non-thread-safe) version of the P4Java RPC
	    implementation to fully support the 'p4jrpcnts' protocol.
	    This implementation allows multiple commands to be excuted
	    over the same RPC connection. To use this implementation,
	    simply connect to the Perforce server using the 'p4jrpcnts'
	    protocol (i.e. 'p4jrpcnts://perforce:1666'). See the Javadoc
	    of the 'NtsServerImpl' class for proper 'safe' usage.

	#437207 (Job #41786, 53896)
	    Added 'p4 -Ztrack' support for 'track' information on what
	    tables a command is accessing, and implicitly locking. This
	    feature is supported at various lower level "raw" IServer
	    methods (i.e. execMapCmd()). To enable 'Ztrack' set the
	    "com.perforce.p4java.enableTracking" or "enableTracking"
	    property to "true" (or anything) when requesting an
	    IOptionServer from the ServerFactory class (as the Properties
	    'props' parameter pass to the getOptionsServer() method).

Minor new functionality in 2012.1

	#467236 (Job #56219)
	    Added the addIfAdmin option to UpdateUserGroupOptions; corresponds
	    to the '-A' option in p4 group. This enables a user with 'admin'
	    access to add a new group. Existing groups may not be modified
	    when this flag is used.

	#403936 (Job #51803)
	    Added new and missing server info fields to IServerInfo:
	    "serverEncryption", "proxyEncryption", "proxyAddress",
	    "proxyRoot", "brokerAddress", "brokerEncryption",
	    "brokerVersion", "peerAddress", "password".

Bugs fixed in 2012.1

	#438221, 438596 (Job #50447, 54015)
	    Fixed problem with 'p4 dirs' not returning error messages
	    when the specified depot directories don't exist. Allow
	    users to turn on/off tagged output per-command via the
	    "inMap". Added handling of both "tagged" and "non-tagged"
	    fields ("dir" and "dirName") returned from the Perforce.

	#446371 (Job #55092, 55236)
	    Fixed P4Java RPC network communication delay problem 'talking'
	    to the Perforce server. Enabled socket 'TCP_NODELAY' (disabled
	    Nagle's algorithm) by default. To turn off the 'TCP_NODELAY',
	    set the "com.perforce.p4java.rpc.tcpNoDelay" or "tcpNoDelay"
	    property to "false" when requesting an IOptionsServer from
	    the ServerFactory class (as the Properties 'props' parameter
	    pass to the getOptionsServer() method).

	#464078 (Job #55266)
	    Fixed issue with RPC socket connection pooling mechanism not
	    being initialized in OneShotServerImpl.

	#464056 (Job #56059)
	    Fixed problem with IOptionsServer.execInputStringMapCmd method
	    where the standard input ("inString" parameter) is being ignored
	    in NtsServerImpl.

-------------------------------------------
Major new functionality in 2011.2

	#355465 (Job #46695)
	    Added streams support for 'p4 diff2'; added the stream and
	    parentStream options to GetFileDiffsOptions; corresponds to
	    the "-S stream" and "-P parent" options in p4 diff2.

	#365153 (Job #46694)
	    Added streams support for 'p4 copy'; added the stream and
	    parentStream options to CopyFilesOptions; corresponds to the
	    "-S stream" and "-P parent" options in p4 copy.

	#357762 (Job #46696)
	    Added streams support for 'p4 dirs'; added the stream and
	    parentStream options to GetDirectoriesOptions; corresponds
	    to the "-S stream" and "-P parent" options in p4 dirs.

	#365890 (Job #46697)
	    Added streams support for 'p4 interchanges'; added the stream
	    and parentStream options to GetInterchangesOptions; corresponds
	    to the "-S stream" and "-P parent" options in p4 interchanges.

	#365817 (Job #46698)
	    Added streams support for 'p4 integrate'; added the stream
	    and parentStream options to IntegrateFilesOptions; corresponds
	    to the "-S stream" and "-P parent" options in p4 integrate.

	#358488 (Job #43229)
	    Implemented the 'p4 logtail' command as the getLogTail method
	    on IOptionsServer. This method outputs the last block(s) of
	    the errorLog and the offset required to get the next block
	    when it becomes available.

	#343002 (Job #47563)
	    Implemented support for bypassing the writing of the ticket on
	    the client machine's disk; instead, return it on the passed-in
	    StringBuffer. Added dontWriteTicket option to LoginOptions;
	    corresponds to '-p' option in p4 login.

	#346153 (Job #46693)
	    Implemented support for limiting output to the client workspaces
	    dedicated to the stream. Added stream option to GetClientsOptions;
	    corresponds to '-S' option in p4 clients.

	#348126 (Job #46692)
	    Implemented support for creating a new branch spec using a
	    specified stream's internally generated mapping. Added stream
	    and parentStream options to GetBranchSpecOptions; corresponds
	    to '-S' and -P options in p4 branch.

	#346149 (Job #46690)
	    Implemented support for creating a new client spec dedicated
	    to a stream; corresponds to '-S' option in p4 client. Updated
	    IClientSummary to include stream support.

	#350458 (Job #46687)
	    Implemented the 'p4 istat' command as the
	    getStreamIntegrationStatus method on IOptionsServer.
	    This methods shows a stream's cached integration status
	    with respect to its parent. If the cache is stale, either
	    because newer changes have been submitted or the stream's
	    branch view has changed, 'p4 istat' checks for pending
	    integrations and updates the cache before showing status.

	#343923 (Job #46686)
	    Implemented the 'p4 streams' command as the getStreams method
	    on IOptionsServer. This methods list of all streams currently
	    known to the system. If a 'streamPath' is specified, the list
	    of streams is limited to those matching the supplied path.

	#344806 (Job #46685)
	    Implemented the 'p4 stream' command as the createStream,
	    deleteStream, getStream and updateStream methods on
	    IOptionsServer. These methods are use to create, delete,
	    retrieve and modify a stream specification. A stream
	    specification ('spec') names a path in a stream depot to
	    be treated as a stream. The spec also defines the stream's
	    lineage, its view, and its expected flow of change.
	    (See 'p4 help streamintro').

	#339164 (Job #46668)
	    Implemented the 'p4 diskspace' command as the getDiskSpace
	    method on IOptionsServer. This method returns summary
	    information about the current availability of disk space
	    on the server.

	#353798 (Job #46667)
	    Implemented the 'p4 merge' command as the mergeFiles method
	    on IClient. This method merges changes from one set of files
	    (the 'source') into another (the 'target'). It is a simplified
	    form of the 'p4 integrate' command.

	#339188 (Job #44327)
	    Implemented the 'p4 protect' command as the
	    createProtectionEntries and updateProtectionEntries
	    methods on IOptionsServer. These methods modify the
	    protections table on the server and control Perforce
	    permissions (users' access to files, commands, etc.).

	#346423 (Job #41780, 41824)
	    Implemented the 'p4 obliterate' command as the obliterateFiles
	    method. This method removes files and their history from the
	    depot permanently, precluding any possibility of recovery.

	#327131 (Job #46113)
	    Added branchResolves, deleteResolves and skipIntegratedRevs
	    options to IntegrateFilesOptions; corresponds to '-R<flags>'
	    options in p4 integrate.

	#326034 (Job #46102)
	    Added resolveFileBranching, resolveFileContentChanges,
	    resolveFileDeletions, resolveMovedFiles and
	    resolveFiletypeChanges options to ResolveFilesAutoOptions;
	    corresponds to '-A<flags>' options in p4 resolve.

	#380305 (Job #37798)
	    Added support for filtering rules to allow the return of
	    untranslated data (byte[]s instead of Strings) from the
	    getExportRecords() method. Currently, the filtering rules
	    are enabled for the 'p4 export' command.

Minor new functionality in 2011.2

	#367652 (Job #46825)
	    Added the caseInsensitiveNameFilter option to the
	    GetClientsOptions, GetBranchSpecsOptions and GetLabelsOptions;
	    corresponds to the '-E' option in p4 clients, branches and
	    labels. This option allows case-insensitive name filtering
	    for the above mentioned commands.

	#370571 (Job #46826)
	    Added new login method signature to support the "login [user]"
	    command argument. This method allows the logged in superuser
	    to request login for another user.

	#343002 (Job #47563)
	    Added the dontWriteTicket option to LoginOptions; corresponds
	    to the '-p' option in p4 login. This allows the return of
	    the auth ticket in the StringBuffer parameter of the login
	    method, and not writing it to the ticket file on disk.

	#374233 (Job #34333)
	    Added the forceTextualMerge, ignoreWhitespaceChanges,
	    ignoreWhitespace and ignoreLineEndings options to
	    ResolveFilesAutoOptions; corresponds to the '-t -db -dw -dl'
	    options in p4 resolve.

	#358886 (Job #46233)
	    Added support for constructing a FileSpec object with the
	    result map from the "dirs" command output.

	#374234 (Job #43124)
	    Surfaced the lower-level map error/info/warning map processors
	    in the IOptionsServer interface. See the IOptionsServer's
	    getErrorStr, getErrorOrInfoStr and getInfoStr methods for
	    usage details.

	#337237 (Job #46682)
	    Added the switchClientView method to IOptionsServer to
	    support switching an existing client spec's view to a view
	    defined in another client spec; corresponds to the '-s -t'
	    options in p4 client.

	#336310 (Job #46671)
	    Added support for the shelvedChange field in IFileSpec.
	    This new field will be captured from the result returned
	    by the unshelveFiles and unshelveChangelist methods.

	#326482 (Job #46114)
	    Added support for a new 'operator' user type in IUserSummary.

	#330020 (Job #46578)
	    Added showTime option to GetLabelsOptions;
	    corresponds to the '-t' option in p4 labels.

	#330002 (Job #46577)
	    Added showTime option to GetBranchSpecsOptions;
	    corresponds to the '-t' option in p4 branches.

	#330006 (Job #46572)
	    Added showTime option to GetClientsOptions;
	    corresponds to the '-t' option in p4 clients.

	#329970 (Job #46091)
	    Added safetyCheck option to SyncOptions;
	    corresponds to the '-s' option in p4 sync.

	#326140 (Job #46086)
	    Added bypassClientDelete option to DeleteFilesOptions;
	    corresponds to the '-k' option in p4 delete.

	#326139 (Job #46063)
	    Added maxFiles option to CopyFilesOptions;
	    corresponds to the '-m' option in p4 copy.

	#325939 (Job #46062)
	    Added changelistId option to ResolveFilesAutoOptions;
	    corresponds to the '-c' option in p4 resolve.

	#342836 (Job #44623)
	    Added the update(boolean force) method signature to IClient
	    and the '-f' option to UpdateClientOptions. These changes
	    allow the force update of locked clients.

	#330955 (Job #46677)
	    Added support for the new 'contentResolveType' field in
	    IFileSpec for the 'resolve' command. Possible values are
	    '3waytext', '3wayraw' and '2wayraw'.

Bugs fixed in 2011.2

	#382664 (Job #49938)
	    Fixed problem with content corruption when adding unicode files.

	#378238 (Job #49985)
	    Added checking for the "Password invalid" message and properly
	    throw an AccessException instead of a generic RequestException.

	#375505 (Job #46583)
	    Added catching of unexpected "release2" and "flush2" protocol
	    messages received by P4Java and throw a ProtocolError. This
	    type of errors can happen when connecting through a proxy and
	    the real server is down.

	#368685 (Job #45050)
	    Implemented the IChangelist update(force) method to allow
	    force update of the 'Date' field in a submitted changelist.

	#339171 (Job #42748)
	    Inserted headers (file name	and revision) before each file
	    content in the output from the IServer.getFileContents method
	    when the "noHeaders" option is set to false (default) on
	    GetFileContentsOptions. The result is also capped by the
	    specified file revision. This is equivalent to the 'p4 print'
	    command without the '-q' option.

	#334658 (Job #47134)
	    Added checking for the existing of the "isgroup" field key
	    from the return map so that the ProtectionEntry is properly
	    set to the group type.

	#339091 (Job #46271)
	    Corrected the map key from "type" to "changeType" so that the
	    ChangelistSummary visibility field is properly set with the value
	    from the result map on the IOptionsServer's getChangelists method.

	#347539 (Job #44417, 44472)
	    Implemented the 'p4 passwd' command with options '-O' and '-P'
	    as the changePassword method on IOptionsServer to allow the user
	    to change the password. This method allows the user to have a '#'
	    in the password which is a limitation of updating the user's
	    password using the user's form spec. However, the '-O' and '-P'
	    options are not supported if your Perforce server is using
	    security level 2 or 3.

	#381075 (Job #42443)
	    Updated the IChangelist.submit method to refresh files and jobs
	    from the server before submitting. The user no longer needs to
	    explicitly call the update() or refresh() method before submitting
	    the changelist.

	#397006 (Job #51466)
	    Updated checking for symlinks on a Mac system with case-insensitive
	    comparison of File.getAbsolutePath() and File.getCanonicalPath.

	#396985 (Job #51617)
	    Fixed the issue with the executable bit not set for file type
	    unicode+x (xunicode).

-------------------------------------------
Major new functionality in 2011.1

	#289284 (Job #43532)
	    Added the execInputStringMapCmd and execInputStreamStreamingMapCmd
	    variants of the "raw" exec commands. These allow users to
	    pass in form-like parameters as a string rather than a Map for
	    those commands where this makes sense. These raw exec commands
	    are not recommended for the typical developer or user, and have
	    been supplied for low-level usage by third-party and internal
	    tools developers.

	#286577 (Job #43079)
	    Implemented hash-based integrity checking for sync and related
	    server-to-client file transfers as documented in the main 2010.2
	    Perforce server documentation. This feature can be disabled
	    for all clients on the server side as documented in the Perforce
	    admin documentation, or in individual P4Java clients by setting
	    the PropertyDefs.NON_CHECKED_SYNC property.

	#285717 (Job #42288)
	    Implemented the new p4d 2010.2 'p4 copy' command as the copyFile
	    method on IClient. This method allows for integrate / resolve
	    operations while ignoring past integration history.

	#285717 (Job #43070)
	    Implemented the new p4d 2010.2 'p4 configure' command as the
	    showServerConfiguration and setServerConfigurationValue
	    methods on IOptionsServer. This feature requires superuser
	    permissions.

	#285505 (Job #43186)
	    Added a CoreFactory class to provide a small set of factory
	    methods to create new local core objects with useful or common
	    default values, and optionally also create them on the Perforce
	    server. As a byproduct, most  core implementation classes now
	    also contain factory methods.

	#283676 (Job #41829)
	    Implemented the new p4d 2010.2 server / protocol authentication
	    scheme. This does not require users or developers to change
	    anything from the 2010.1 release.

	#281570 (Job #42788)
	    Implemented a new low-level streaming interface that allows
	    developers to get results one result at a time as they come
	    in from the server (rather than en masse) using a callback.
	    The results are handed to the callback method as a Java map,
	    similar to the low-level execMapCmd (etc.) methods.

	#272759 (Job #41636)
	    Implemented the createDepot method for creating new depots
	    in the repository.

        #273687 (Job #41552)
	    Implemented file attributes on a per-file basis using
	    the getExtendedFileSpecs method.

Minor new functionality in 2011.1

	#289633 (Job #39472)
	    Users of the IOptionsServer getMatchingLines method can
	    now pass in an optional parameter to allow for the retrieval
	    of info and warning messages from the Perforce server in
	    response to things like lines being too long, etc.

	#289590 (Job #43599)
	    Added truncateDescriptions option to GetChangelistsOptions;
	    corresponds to '-L' option in p4 changes.

	#289541 (Job #43581)
	    Added omitNonContributaryIntegrations option to the
	    GetRevisionHistoryOptions class to implement '-s'
	    functionality.

	#289499 (Job #43611)
	    Added support for getting additional Perforce user information
	    with the IOptionsServer getUsers method using the equivalent
	    of the '-l' option in GetUsersOptions.

	#289452 (Job #43610)
	    Added support for retrieving service users with the
	    IOptionsServer getUsers method using GetUsersOptions; the
	    new option corresponds to the '-a' flag on p4 users.

	#288467 (Job #43076)
	    Added support for the new Perforce user 'type' field.

	#288444 (Job #43075)
	    Added support for the new PasswordTimeout field in Perforce
	    user groups (IUserGroup).

	#288276 (Job #43073)
	    Added support for the new 'type' field in changelists; see
	    the documentation for IChangelistSummary for details. Also
	    added related -f option to the IOptionsServer getChangelists
	    method.

	#288206 (Job #43072)
	    Added support for the annotate '-I' option in IOptionsServer.

	#286845 (Job #43157)
	    Added several methods to RequestException and FileSpec to
	    support more extensive error code retrieval for server-
	    generated errors. Use of the added methods is currently
	    considered to be experimental, unsupported, and undocumented,
	    but developers who know what they're doing and need this sort
	    of low-level error subcode access are welcome to try it....

	#285505 (Job #35253)
	    Made it possible to (optionally) issue arbitrary commands
	    through the execMapCmd method with function name checking
	    turned off. This feature is potentially dangerous and should
	    not be used unless you a) need lower-level command support;
	    and b) can cope with the consequences, which may include
	    client- or server-side data corruption.

	#284362 (Job #43123)
	    Added getManifest method to com.perforce.p4java.Manifest
	    to allow users to retrieve the associated jar file
	    manifest (if it exists); changed Manifest's getP4JVersionString
	    and getP4JDateString methods to use the jar manifest version
	    and date attributes if they exist.

	#283970 (Job #42964)
	    Class com.perforce.p4java.Metadata now has a static main
	    entry point that can be used to print out version and other
	    metadata from the enclosing jar file's manifest from the
	    command line or other execution environment.

-------------------------------------------
Major new functionality in 2010.1

	#262626 (Job #39410)
	    Implemented a new global usage options class and feature for P4Java.
	    This feature allows users to set a limited number of options
	    (typically corresponding to options detailed in "p4 help usage"
	    documentation, but not limited to that) that can apply to all
	    commands issued by a server. See the Javadoc commentary for
	    class com.perforce.p4java.options.UsageOptions for details.

	#261838 (Job #39408)
	    Implemented a new Options-class based set of methods and interfaces
	    to allow for forwards and backwards binary compatibility. See the
	    Javadoc comments for class com.perforce.p4java.options.Options
	    and associated documentation for details. This change should have
	    no impact on current users unless they wish to adopt the new
	    options-class-based features.

Minor new functionality in 2010.1

	#265749 (Job #40849)
	    Added undoc max files (-m) option to IServer.getDepotFiles's
	    GetDepotFilesOptions. Note that this is an undoc option and
	    will cause a RequestException if used with servers that do not
	    support the new -m option.

	#262626 (Job #40738)
	    Implemented p4 grep functionality as IOptionsServer's
	    getMatchingLines method.

	#252734 (Job #39466)
	    Implemented IServer.getLoginStatus, corresponding to login -s, to
	    allow users to determine their Perforce login status.

	#263740 (Job #040670)
	    P4Java jar file is now digitally signed.  Certificate information
	    can be verified by using the jarsigner utility.

Bugs fixed in 2010.1

	#266827 (Job #40877)
	    Jobs marked as fixed using server.fixJobs() or an external fix
	    operation now show up properly if an associated changelist is
	    submitted from P4Java. Previously the job fix records were
	    missing after submission.

	#265493 (Job #40829)
	    Sync operations on IClient objects that are not the current client
	    of the active server object now cause a suitable RequestException
	    to be thrown. Previous behavior was to silently sync whichever
	    client was currently attached to the server (if any).

	#263341 (Job #38602)
	    Copyright symbols in text file no longer cause automatic file type
	    detection to set the file type to binary. Note that P4Java file type
	    inference is always probabilistic, and may cause problems with
	    certain non-ASCII character sets, and -- in general -- users should
	    always use type maps or explicit type specifiers where there's any
	    chance of ambiguity.

	#263794 (Job #39015)
	    P4Java streams-based commands (principally file diff and file
	    content retrieval methods) now correctly throw exceptions when
	    the server denies access due to login session expiry, etc. This
	    is also now true for any server-detected error.
	    Previous versions simply returned null streams regardless of
	    the error.

	#263551 (Job #39525)
	    Checking out a file that doesn't exist in the client workspace now
	    gives a useful status message. Previous versions were silent when
	    they detected the missing file.

	#263826 (Job #40205)
	    Syncing to rev #0, fspec.getAction() on successfully-deleted filespecs
	    now returns "deleted" instead of null.

	#264092 (Job #40346)
	    Base file name and revision is now available from IClient.integrateFiles
	    output when IntegrationOptions.setDisplayBaseDetails(true) is
	    used.

	#263522 (Job #40601)
	    IClient.resolveFile now correctly resolves binary files. Previous
	    implementation ignored attempts to resolve binary files.

	#259993 (Job #39437)
	    IMapEntry.getRight(true) now returns correctly quoted right side
	    entry string.

-------------------------------------------
Major new functionality in 2009.3

	#234216 (Job #37582)
	    The undoc interchanges command has been implemented in P4Java.

	#231905 (Job #37568)
	    Diff2 command implemented in P4Java.

	#231806 (Job #37558)
	    Job deletion now available through P4Java.

	#220561 (Job #32576)
	    Implemented zeroconf-based browsing in P4Java based on JmDNS.
	    Note that JmDNS is not bundled with P4Java -- it just has to be
	    on the classpath somewhere for zeroconf to work; if it's not there,
	    you just can't do zeroconf browsing.

Minor new functionality in 2009.3

	#245412 (Job #38974)
	    Added a new System property, PropertyDefs.WRITE_IN_PLACE_KEY, that
	    if set to "true", will allow sync file writes to be performed in
	    place rather than to a temporary file (which is then renamed). The
	    motivation here is that on Windows systems, the JVM does not do
	    atomic renames properly (see Sun's bug ID 4017593 for a discussion
	    of this), which can have a performance impact with forced syncs
	    when a file already exists. Do not use this feature unless you
	    have been advised to by Perforce support personnel, and / or you
	    are fully aware of the consequences of doing this, which include
	    possible partial reads of the target file(s) during the sync or
	    improper file contents after network problems. Both cases are
	    highly unlikely, and in any case no data corruption will occur.

	#238378 (Job #38059)
	    IServerInfo now has a method that returns the Proxy Server version
	    if a proxy is being used.

	#234798 (Job #37750)
	    Added IServer.isCaseSensitive method to test if a Perforce server
	    is case sensitive or not. This should return true on Linux servers
	    and false on Windows servers.

	#248702 (Job #039041)
	    Support reusing sockets for multiple commands run against an
	    IServer object. com.perforce.p4java.rpc.socketPoolSize can now be
	    configured as a property specified to the ServerFactory class when
	    requesting a server that will control how many sockets are retained
	    when a command completes to be used by subsequent commands.  This
	    option is off by default and a value of greater than zero must be
	    set to turn on socket pooling.  A system property,
	    com.perforce.p4java.RPC_SOCKET_IDLE_TIME, now controls how much
	    time a socket can be idle for before it is closed by the client.

Bugs fixed in 2009.3

	#262259 (Job #40201)
	    Tightened up file / stream / socket close logic and error
	    reporting as a side effect of fixing bug #40241. This should
	    help Perforce support debug left-open system file descriptor
	    issues and reduce P4Java resource usage.

	#261907 (Job #40241)
	    In certain cases of binary file transfers during sync operations,
	    file CRC values were incorrectly logged as "bad". The file
	    contents were correctly transferred, but sockets and / or
            file handles were being left open in P4Java as a side-effect.

	#253486 (Job #039486)
	    Submit failure was not handled properly; in some situation, it
	    was possible to submit files despite submit	failure.

	#253486 (Job #039304)
	    Users erroneously saw a "A revision specification (# or @) cannot
	    be used here." message when using IServer.getProtectionEntries().

	#248698 (Job #039012)
	    IFileAnnotation.getLine(boolean processLineEndings) no longer
	    returns incorrect line endings when running annotate against a
	    Windows server.  Previously when true was specified as the first
	    parameter to a Windows server the String returned would contain
	    an extra '\r' character.

-------------------------------------------
Major new functionality in 2009.2

	#216495 (Job #35310)
	    P4Java type and method names refactored to conform to the following
	    general rules:

	    * "P4J" prefix deleted;
	    * Use "I" as interface name prefix;
	    * "Impl" suffix for impl classes deleted;
	    * Regularize "ChangeList" to "Changelist";
	    * Deleted "List" and "Summary" suffixes for methods, eg.
	      getClientSummaryList -> getClients;
	    * Normalized "new" prefix to "create" prefix, e.g.
	      newClient -> createClient.

	#214537 (Job #35428)
	    P4Java public interface definitions now include setter methods
	    as well as getters for all public fields.

	#216787 (Job #35545)
	    P4Java filespec-related interfaces have been refactored to simplify
	    access and functional relationships.

	#216659 (Job #35550)
	    IServerResource-based resource (clients, jobs, changelists,
	    labels, and branches) interfaces and associated functionality
	    have been refactored to syntactically and semantically distinguish
	    between "summary" and "full" versions of the associated objects.

	#216095 (Job #35674)
	    View-based objects (clients, labels, branches, etc.) now use a
	    unified view map class (ViewMap) as the basis for all view map
	    functionality.

	#216679 (Job #32564)
	    P4Java now supports the full creation / deletion / updating
	    of Perforce users through the IServer interface.

	#216828 (Job #35810)
	    P4Java now supports the full creation / deletion / updating
	    of Perforce user groups through the IServer interface.

	#211525 (Job #34925)
	    P4Java now supports the annotate command for files under
	    its control.

	#216488 (Job #35239)
	    P4Java now supports the "protects" command.

	#216531 (Job #35797)
	    P4Java now supports counter create / set / delete operations.

	#216555 (Job #35801)
	    P4Java now supports the reviews command.

	#211334 (Job #35000)
	    P4Java now supports the dbschema and export commands (with
	    documented limitations).

Minor new functionality in 2009.2

	#216412 (Job #35081)
	    P4Java now supports the "-k" option to the move and edit
	    commands.

	#216463 (Job #35444)
	    P4Java now supports the -a option to login.

	#216876 (Job #34567)
	    IServer.deletePendingChangelist now returns a status string on
	    completion.

	#216871 (Job #34596)
	    IServer.deleteClient now returns a status string on completion.

Bugs fixed in 2009.2

	#240240 (Job #38402)
	    P4Java now reports the have revision correct under all
	    circumstances when using the file spec operations. Previous
	    releases occasionally showed the have revision as either higher
	    or lower than the actual have revision.

	#240240 (Job #38015)
	    P4Java now correctly releases file handles on sync and other
	    file operations with binary files. As a side effect, P4Java
	    should no longer log "Bad PKZIP trailer length" messages
	    on syncs and other file operations. Note that even when the
	    file handles were not released or the bad PKZIP trailer
	    messages appeared, data was being correctly transferred
	    and file contents were not being affected.

	#240240 (Job #38231)
	    Added IProtectionEntry excluded path methods to detect and / or
	    set excluded paths for protection entries.

	#240240 (Job #37980)
	    IServer.getProtectionEntries now returns correct data when
	    used with a file specification parameter.

	#238859 (Job #37970)
	    P4Java now works correctly with proxied servers when local
	    proxy caches are out of date.

	#238858 (Job #37980)
	    P4Java now returns correct protection entries when used with
	    file specification arguments.

	#232364 (Job #37598)
	    P4Java now sets a file's head modification time correctly on
	    submits; this value is now available properly when using the
	    getExtendedFileSpecs method on the IServer interface.

	#231471 (Job #37521)
	    P4Java now does a better job of detecting known binary file types
	    and Unicode files on both Unicode and non-Unicode servers.
	    Please note, however, that customers are always strongly
	    encouraged to use explicit type maps for all file types,
	    as file type inference in the absence of type maps or
	    -t flags (etc.) is heuristic and is not guaranteed to
	    get the "correct" answer in all circumstances.

	#228643 (Job #36781)
	    P4Java's Metadata class methods getP4JVersionString() and
	    getP4JDateString() now return P4Java release version and date
	    metadata useful for debugging (note that the 2009.2 Metadata
	    class was known as the P4Java class in previous releases).

	#228609 (Job #37128)
	    P4Java can now communicate properly with the Perforce server
	    when used with the tr_TR locale.

	#226748 (Job #36923)
	    Fixed bug where merge operations sometimes resulted in a
	    ConnectionException with the message "No metadata defined for
	    RPC function encoding: client-OpenMerge2". As a side effect,
	    two-way merges are now implemented in P4Java, allowing for
	    (safe) merges on binary files.

	#225502 (Job #36870)
	    P4Java now properly handles submits of files that don't exist
	    on the local client; the error message no longer mentions the
	    "lbr-ForsakeFile" function.

	#219483 (Job #36074)
	    Clients with no Host field updated through P4Java no longer
	    have the Host field set to 'null' after the update.

	#217317 (Job #35376)
	    P4Java authentication ticket system now checks properties for
	    relevant property definitions when saving a ticket value.

	#217319 (Job #35378)
	    Tickets files used by P4Java are no longer set read-only with JDK5
	    and / or Windows systems.

	#217322 (Job #35850)
	    When marking a JAR file for add, P4Java correctly detects the type
	    as binary (this is true now for PKZIP-derived files in general).

	#216522 (Job #35104)
	    IServer.execMapCmd(CmdSpec.INFO.name(), ... ) correctly down-cases
	    the command name with the RPC implementation.

	#216289 (Job #35240)
	    P4Java correctly manages an empty authentication tickets file on
	    Windows platforms.

	#216261 (Job #35252)
	    P4Java now uses an 'unset' value for client names rather than
	    defaulting to local host names when no client is set.

	#216315 (Job #35290)
	    P4Java now correctly parses and respects the IFileSpec endRevision
	    field value with file history commands.
