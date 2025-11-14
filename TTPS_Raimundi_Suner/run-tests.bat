@echo off
REM Script to run tests for TTPS_Raimundi_Suner project

echo ================================================
echo Running Tests for TTPS_Raimundi_Suner
echo ================================================
echo.

REM Check if JAVA_HOME is set
if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set!
    echo Please set JAVA_HOME to your JDK 21 installation directory
    echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-21
    echo.
    pause
    exit /b 1
)

echo Using Java from: %JAVA_HOME%
echo.

REM Change to project directory
cd /d "%~dp0"

REM Run Maven tests using the wrapper
echo Running Maven tests...
echo.
call mvnw.cmd clean test

echo.
echo ================================================
echo Test execution completed!
echo ================================================
echo.
pause

