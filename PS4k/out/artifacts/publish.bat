del "%CD%\webdata\main.jar"
copy "%CD%\PS4k_jar\main.jar" "%CD%\webdata\main.jar"
del "%CD%\webdata\images.jar"
copy "%CD%\PS4k_jar\images.jar" "%CD%\webdata\images.jar"
rmdir /s /q "%CD%\webdata\lib" 
xcopy /i "%CD%\PS4k_jar\lib" "%CD%\webdata\lib"
java -classpath "%CD%\webdata\getdown-client-new.jar" com.threerings.getdown.tools.Digester "%CD%\webdata\
"C:\Users\flux\Dropbox\tools\WinSCP\WinSCP.exe" /console /script=upload-script.txt