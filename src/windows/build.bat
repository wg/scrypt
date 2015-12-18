set local enableextensions

@echo Building libscrypt.dll for x86
call setenv /Release /x86
mkdir ..\..\target\native\win32
cl /O2 /EHs /MD /Iinclude /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" /c /Fo..\..\target\native\win32\ /DHAVE_CONFIG_H c\*.c 
link /DLL /nologo /MACHINE:x86 /OUT:..\..\target\native\win32\libscrypt.dll ..\..\target\native\win32\*.obj 
copy /Y ..\..\target\native\win32\libscrypt.dll ..\..\target\libscrypt32.dll

@echo Building libscrypt.dll for x64
call setenv /Release /x64
mkdir ..\..\target\native\win64
cl /O2 /EHs /MD /Iinclude /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" /c /Fo..\..\target\native\win64\ /DHAVE_CONFIG_H c\*.c 
link /DLL /nologo /MACHINE:x64 /OUT:..\..\target\native\win64\libscrypt.dll ..\..\target\native\win64\*.obj 
copy /Y ..\..\target\native\win64\libscrypt.dll ..\..\target\libscrypt64.dll

endlocal

