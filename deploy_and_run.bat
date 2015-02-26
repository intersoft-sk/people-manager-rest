
rd /q /s "%~dp0tomcat\webapps\pmrest"
del "%~dp0tomcat\webapps\pmrest.war"

copy "%~dp0target\pmrest.war" "%~dp0tomcat\webapps"

set "CATALINA_HOME=%~dp0tomcat"
set "EXECUTABLE=%~dp0tomcat\bin\catalina.bat"
call "%EXECUTABLE%" run
