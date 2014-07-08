=====================================================
             How to run examples
=====================================================
1  UNIX/LINUX: Make sure the shell files are executable and permissions are set: (chmod +x *.sh) in julp-examples and julp-examples/db/hsqldb/bin 
2. Run hsqldb server first(julp-examples/db/hsqldb/bin/runServer.bat/runServer.sh)
3. Make sure to adjust size of commandline window to fit examples output.
4. Run example. Every example before running restores database to default (julp-examples/db/hsqldb/setup.sql).
5. Optionally you can run hsqldb GUI Database Manager (julp-examples/db/hsqldb/bin/runManager.bat/runManager.sh) to see the data.
   When you open Connection Dialog make sure to select HSQLDB Database Engine Server, not HSQLDB Database Engine In-Memory or anything else.
   User: sa 
   no password
   
6. Oracle and Firebird examples are located in julp-examples/oracle and julp-examples/firebird.
   You will have to create and setup databases for oracle/firebird and add jdbc drivers to ProjectJulp-3/lib/ext.
   For Firebird you need to create alias. See julp-examples/db/firebird/aliases.conf

=====================================================
The examples are using HSQLDB (http://hsqldb.org)
=====================================================
