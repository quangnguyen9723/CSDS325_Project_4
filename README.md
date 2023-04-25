# CSDS325_Project_4

HTTP Proxy Server

Steps to run the program:

* First, we need to compile the files


    cd src/
    javac *.java

* After that, we can run the Server:


    java ProxyServer <port>
(note that the port is optional, if not specified, the default port is 8080)

To test the program, we can do the following:

    export http_proxy=http://127.0.0.1/<port> (port the proxy is running on)
    wget <http url>

    
