[![Support](https://img.shields.io/badge/Support-Official-green.svg)](mailto:support@perforce.com)

# P4Java Examples

P4Java is officially supported by Perforce.  Pull requests will be managed by Perforce's engineering teams.  We will do our best to acknowledge these in a timely manner based on available capacity.  Issues will not be managed on GitHub.  All issues should be recorded via Perforce's standard support process (https://www.perforce.com/support/request-support). 

## Overview
P4Java is a Java-native API for accessing Perforce SCM services from within Java applications, servlets, plugins, and other Java contexts.  

## Requirements
* Perforce server at Release 2015.1 or higher.
* Java: full standard JDK 1.8 or later.  Implementation as discussed in "Known Limitations" below.
* SSL: unlimited strength JCE (Java Cryptography Extension) package for 256-bit encryption level SSL connection to a secure Perforce server.

### SSL and Trust

Perforce server 2015.1 or higher supports 256-bit SSL connections and trust establishment via accepting the fingerprint of the SSL certificate's public key. The standard JDK comes with 128-bit encryption level ciphers. In order to use P4Java to connect to a secure Perforce server, you must download and install the unlimited strength JCE package for your JDK version.

To make a secure connection using P4Java, simply append 'ssl' to the end of the P4Java protocol (i.e. 'p4javassl://perforce:1667').  For new a connection or a key change, you must also (re)establish trust using the IOptionsServer's 'addTrust' method.  See example code snippet below:

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

P4Java 2020.1 has changed the default TLS support to TLSv1.2 for use with Perforce server 2019.1 or greater this can be modified using the JVM option:

    java -DsecureSocketEnabledProtocols=TLSv1.2 

## Documentation
For more information please refer to the Helix Core P4Java Developer Guide https://www.perforce.com/manuals/p4java/Content/P4Java/Home-p4java.html

## Support
This project is maintained by Perforce Engineering and fully supported.  Pull requests will be managed by Perforce's engineering teams.  We will do our best to acknowledge these in a timely manner based on available capacity.  Issues will not be managed on GitHub.  All issues should be recorded via Perforce's standard support process (https://www.perforce.com/support/request-support). 

## Usage
The example snippets in this project require P4Java (2019.1 or later) available from Maven Central (`https://repo1.maven.org/maven2/`).  Use one of the following tools to add the dependancy.

### Maven
    <dependency>
        <groupId>com.perforce</groupId>
        <artifactId>p4java</artifactId>
        <version>2019.1.1939255</version>
    </dependency>

### Gradle
    compile group: 'com.perforce', name: 'p4java', version: '2019.1.1939255'
