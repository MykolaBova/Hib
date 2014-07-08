@echo off
setlocal

java -cp .;..\lib\julp-examples.jar;..\db\hsqldb\lib\hsqldb.jar;..\..\lib\julp.jar;..\..\lib\julp-search.jar;..\..\lib\julp-util.jar;..\..\lib\ext\cglib-nodep-3.1.jar;..\..\lib\ext\jxl.jar;..\..\lib\julp-gui.jar org.julp.examples.JulpExamplesMain -dorg.hsqldb.jdbcDriver -rjdbc:hsqldb:hsql://localhost/julp_examples -usa -f..\db\hsqldb\setup.sql -ecreateAndStoreCustomersNoExtending


endlocal
pause
