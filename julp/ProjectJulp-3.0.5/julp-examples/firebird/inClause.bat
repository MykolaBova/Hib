@echo off
setlocal


java -cp ".;..\lib\*;..\..\lib\*;..\..\lib\ext\*" org.julp.examples.JulpExamplesMain -dorg.firebirdsql.jdbc.FBDriver -rjdbc:firebirdsql:localhost/3050:julp?defaultHoldable=true -ujulp -pjulp -f..\db\firebird\setup.sql -eINClause


endlocal
pause