@echo off
setlocal

java -cp .;..\..\lib\julp-ext.jar;..\lib\julp-examples.jar;..\..\lib\julp.jar;..\..\lib\julp-search.jar;..\..\lib\julp-util.jar;..\..\lib\ext\cglib-nodep-3.1.jar;..\..\lib\ext\jxl.jar;..\..\lib\julp-gui.jar org.julp.examples.JulpExamplesMain -d -r -f -u -eloadFromXlsFile


endlocal
pause