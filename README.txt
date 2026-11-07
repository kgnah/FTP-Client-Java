												FTP Client Java Project
Project Overview:
    This project implements a graphical FTP Client in Java that can interact with a public FTP server such as ftp.gnu.org.

Features
	- Connect to FTP server using default port 21.
	- Anonymous login and custom username/password login.
	- PWD command.
	- CWD command.
	- LIST using PASV + LIST.
	- RETR download using PASV + RETR.
	- STOR upload using PASV + STOR.
	- DELE delete file.
	- MKD create directory.
	- RMD remove directory.
	- QUIT disconnect from server.

Libraries Used
   Networking and I/O
	- java.net.*
	- java.io.*

GUI
	- javax.swing.*
	- java.awt.*

Project Structure:
	FTPClientProject/
	│
	├── GUI.java
	├── FTPClient.java
	├── README.txt
	└── Report.docx

Build Instructions
   Open terminal/command prompt inside the project directory.
   Compile the project:
	javac GUI.java FTPClient.java

Run Instructions
   Run the application:
	java GUI
   The FTP Client GUI window will appear.

Default Public FTP Server
   Default configuration:
	- Host: ftp.gnu.org
	- Username: anonymous
	- Password: anonymous

   The public FTP server can be used to test:
	- LIST
	- PWD
	- CWD
	- RETR
  Note:
    Public FTP servers usually disable writable operations for security reasons such as:
	- STOR
	- DELE
	- MKD
	- RMD

Integrity Declaration
    I confirm that this submission is my own work. I did not copy code from AI tools, classmates, or online repositories.