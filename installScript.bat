set installDir=C:\Program Files\Micro-Manager-2.0gamma_new
echo copy dependencies
xcopy /s ".\bindist" "%installDir%"
:: xcopy ".\PWS.lnk" "%USERPROFILE%\Desktop"

xcopy ".\scripts\StartupMacros.txt" "%installDir%\macros\StartupMacros.txt"

set SCRIPT="%TEMP%\%RANDOM%-%RANDOM%-%RANDOM%-%RANDOM%.vbs"

@echo off 

echo Set oWS = WScript.CreateObject("WScript.Shell") >> %SCRIPT%
echo sLinkFile = "%USERPROFILE%\Desktop\myshortcut.lnk" >> %SCRIPT%
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> %SCRIPT%
echo oLink.TargetPath = "%installDir%\ImageJ.exe" >> %SCRIPT%
echo oLink.Arguments = "-eval ""run('Micro-Manager Studio', '-profile PWS');""" >> %SCRIPT%
echo oLink.Save >> %SCRIPT%

cscript /nologo %SCRIPT%
del %SCRIPT%

pause