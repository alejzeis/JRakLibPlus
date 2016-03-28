# JRakLibPlus [![Travis](https://img.shields.io/travis/jython234/JRakLibPlus?style=flat-square)](https://travis-ci.org/jython234/JRakLibPlus)
A library for easy creation of RakNet servers, based on RakLib and JRakLib.

##### Currently supported features:
 - Base RakNet protocol 7
   - Packets 0x01-0x08
   - Packets 0x09-0x13
   - Reliable Datagram transport with reliability
     - CustomPackets 0x80-0x8F
     - ACK
     - NACK
 - Easy integration with server implementations
 
#### How to use
JRakLibPlus uses maven for dependencies. To build from source you need Oracle JDK 8 and Maven 3+. 
To build the JAR simply run ```mvn package```
##### Using from maven
To include the library in your application add the following to your POM:
```
<repositories>
        <repository>
            <id>snapshot-repo</id>
            <url>https://raw.githubusercontent.com/BlockServerProject/MavenRepository/master/snapshots</url>
        </repository>
</repositories>
...
<dependencies>
        <dependency>
            <groupId>io.github.jython234.jraklibplus</groupId>
            <artifactId>JRakLibPlus</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
</dependencies>
...
```
