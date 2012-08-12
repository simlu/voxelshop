- "publish.bat" contains absolute filepath to WinSCP

- To add new Jars:
Add them to the lib folder "modules" (if new subdir add that to dependencies, for intellij compilation).
Add to the artifact build "lib" (jar, so they are exported)
Add to classpath (project settings)
Add to GetDown getdown.txt