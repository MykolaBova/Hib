@echo off
setlocal


java -cp .;..\lib\julp-examples.jar;..\db\oracle\ojdbc6.jar;..\..\lib\julp.jar;..\..\lib\julp-search.jar;..\..\lib\julp-util.jar;..\..\lib\ext\asm-3.1.jar;..\..\lib\ext\cglib-2.2.jar;..\..\lib\ext\jxl.jar;..\..\lib\julp-gui.jar org.julp.examples.JulpExamplesMain -doracle.jdbc.OracleDriver -rjdbc:oracle:thin:@localhost:1521:XE -ujulp -pjulp -f..\db\oracle\setup.sql -ecreateAndStoreCustomersNoExtending


endlocal
pause
