::****************************************************
::*     Standard Java Development Initialization     *
::****************************************************
@set PATH="C:\Program Files\Java\jdk1.8.0_221\bin";%PATH%
	@rem echo permenent classpath=%CLASSPATH%
	@set CLASSPATH=%CLASSPATH%;d:/apps/src
	@rem echo modified classpath=%CLASSPATH%
	@set JCOPTS=-Xlint:unchecked
	@rem set JCOPTS=-g:none -nowarn
	@set JC=javac -cp ".;*" %JCOPTS%
javac -version
java -version
	cd \apps\src

::****************************************************
::*       Java Project Specific Initialization       *
::****************************************************
	@set SRC_ROOTDIR=com\shanebow\web
	@set USR_ROOTDIR=d:\apps\usr\shanebow\tools
	@set USR_ADMINDIR=%USR_ROOTDIR%\admin
	@set USR_CLIENTDIR=%USR_ROOTDIR%

	@set APP=SiteBuilder
	@set SRC_DIR=%SRC_ROOTDIR%\%APP%
	@set JAR_FILE=%APP%.jar

	@if not "%1" == "" goto %1

:no_command_specified
	@call :newrunnablejar %JAR_FILE% %SRC_DIR% %APP%
	@call :compiletojar %JAR_FILE% com\shanebow\ui\wizard

:filevisitor
	@call :compiletojar %JAR_FILE% com\shanebow\tools\filevisitor
	@if not "%1" == "" goto heaven

:uniedit
	@call :compiletojar %JAR_FILE% com\shanebow\tools\uniedit
	@call :compiletojar %JAR_FILE% com\shanebow\tools\dirsearch
	@call :compiletojar %JAR_FILE% com\shanebow\ui\image
	@if not "%1" == "" goto heaven

:dbm_run
	@call :compiletojar %JAR_FILE% com\shanebow\web\host
	@call :compiletojar %JAR_FILE% %SRC_DIR%\dbm
	@if "%1"=="dbm_run" call :copytoclient %JAR_FILE%
	@if "%1"=="dbm_run" call :runclient %JAR_FILE%
	@if not "%1" == "" goto heaven

:ftp
:ftp_run
	@call :compiletojar %JAR_FILE% com\shanebow\web\ftp
	@if "%1"=="ftp_run" call :copytoclient %JAR_FILE%
	@if "%1"=="ftp_run" call :runclient %JAR_FILE%
	@if not "%1" == "" goto heaven

:links
:links_run
	@call :compiletojar %JAR_FILE% %SRC_DIR%\links
	@if "%1"=="links_run" call :copytoclient %JAR_FILE%
	@if "%1"=="links_run" call :runclient %JAR_FILE%
	@if not "%1" == "" goto heaven

:editor
:editor_run
::@call :compiletojar %JAR_FILE% com\shanebow\tools\uniedit
	@call :compiletojar %JAR_FILE% %SRC_DIR%\editor
	@if "%1"=="editor_run" call :copytoclient %JAR_FILE%
	@if "%1"=="editor_run" call :runclient %JAR_FILE%
	@if not "%1" == "" goto heaven

:build
:build_run
	@call :compiletojar %JAR_FILE% %SRC_DIR%\imp
	@call :compiletojar %JAR_FILE% %SRC_DIR%\style
	@call :compiletojar %JAR_FILE% %SRC_DIR%\build
	@if "%1"=="build_run" call :copytoclient %JAR_FILE%
	@if "%1"=="build_run" call :runclient %JAR_FILE%
	@if not "%1" == "" goto heaven

:sitemap
:sitemap_run
	@call :compiletojar %JAR_FILE% %SRC_DIR%\pages
	@call :compiletojar %JAR_FILE% %SRC_DIR%\sitemap
	@if "%1"=="sitemap_run" call :copytoclient %JAR_FILE%
	@if "%1"=="sitemap_run" call :runclient %JAR_FILE%
	@if not "%1" == "" goto heaven

:SiteBuilder
@call :compiletojar %JAR_FILE% com\shanebow\tools\uniedit
	@call :compiletojar %JAR_FILE% %SRC_DIR%\editor
	@call :compiletojar %JAR_FILE% %SRC_DIR%
	@call :classtojar %JAR_FILE% com\shanebow\tools\Expose ActProperties
	@call :copytoclient %JAR_FILE% _sbcommon.jar mysql-connector-java-5.1.13-bin.jar
	@call :copytoclient %JAR_FILE%  _sbdao.jar _filetree.jar _sbthai.jar _sbspider.jar
	@if not "%1"=="" call :runclient %JAR_FILE%

::****************************************************
::*       Standard Java Development Subroutines      *
::****************************************************
goto heaven
::newrunnablejar %JAR_FILE% %SRC_DIR% %APP%
:newrunnablejar
	@echo creating %1...
	@jar cfm %1 %2\%3.mft copyright.txt %2\resources
	@GOTO:EOF

:: compiles a single java file and put class file(s) into jar
:classtojar
	@echo _class %2 %3...
	@%JC% %2\%3.java
		@if not %ERRORLEVEL%==0 goto hell
	@echo __updating %1...
		@if not %ERRORLEVEL%==0 goto hell
	@jar uf %1 %2\*.class
	@rem echo __deleting class files...
	@del %2\*.class > NUL
	@GOTO:EOF

:compiletojar
	@echo _compiling %2...
	@%JC% %2\*.java
		@if not %ERRORLEVEL%==0 goto hell
	@echo __updating %1...
		@if not %ERRORLEVEL%==0 goto hell
	@jar uf %1 %2\*.class
	@echo __deleting class files...
	@del %2\*.class > NUL
	@GOTO:EOF

:copytoadmin
	@rem copies files (params) to the admin user dir
	@echo __copying FILES to %USR_ADMINDIR%...
	@FOR %%B IN (%*) DO @copy %%B %USR_ADMINDIR% > NUL
	@GOTO:EOF

:runadmin
	@cd %USR_ADMINDIR%
	@java -jar %*
		@if not %ERRORLEVEL%==0 pause
	@exit
	@GOTO:EOF

:copytoclient
	@rem copies files (params) to the client user dir 
	@echo __copying FILES to %USR_CLIENTDIR%...
	@FOR %%B IN (%*) DO @copy %%B %USR_CLIENTDIR% > NUL
	@GOTO:EOF

:runclient
	@cd %USR_CLIENTDIR%
	@java -jar %*
	@if not %ERRORLEVEL%==0 pause
	@exit
	@GOTO:EOF

:heaven
	@echo Heaven
	@if "%1" == "" pause
	@exit

:hell
	@echo Error: %ERRORLEVEL%
	@pause
	@exit
