/*
    Project 2 - Basic Web Server
    An implementation of a web server using Java Sockets
    Reference: http://javarevisited.blogspot.com/2015/06/how-to-create-http-server-in-java-serversocket-example.html
*/

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;

public class Server implements Runnable
{
    // port to be used
    protected static int port;
    // server socket
    protected ServerSocket serverSocket;
    // client socket
    protected Socket clientSocket;
    // stream reader
    protected BufferedReader br;
    // client IP
    protected InetAddress clientAddress;

    // some strings for writing the page
    protected static final String pageStart = "<html>\n<head>\n\t<title> Project 2 - Web Server </title>\n</head>\n<body>\n<table border = \"1\">";
    protected static final String pageEnd = "\n</table>\n</body>\n</html>";
    protected static String headerTable = new String ();

    public static void main (String[] args)
    {
        try
        {
            // parse argument as integer
            port = Integer.parseInt (args[0]);
        }
        catch (NumberFormatException nfe)
        {
            // catch conversion issue
            System.out.println ("Invalid input");
        }
        finally
        {
            // create a server instance
            Server server = new Server (port);
        }
    }

    public Server (int port)
    {
        try
        {
            // create server socket from port input
            serverSocket = new ServerSocket (port);
        }
        catch (IOException io)
        {
            System.out.println ("Invalid socket");
        }
        finally
        {
            // run thread
            this.run ();
        }
    }

    @Override
    public void run ()
    {
        try
        {
            // create socket for client
            clientSocket = serverSocket.accept ();
            // get input stream from socket
            br = new BufferedReader (new InputStreamReader (clientSocket.getInputStream ()));
            // flag for counting lines
            int currLine = 1;
            // parse the header
            String line = br.readLine ();
            while (!line.isEmpty ())
            {
                switch (currLine)
                {
                    case 1:
                        String[] params = line.split (" ");

                        // Request Type
                        headerTable += tableRow ("Request Type", params[0]);

                        // check if file exists. append necessary information in said function
                        checkIfFileExists (params[1].replaceFirst ("/", ""));

                        // Protocol
                        headerTable += tableRow ("Protocol", params[2]);
                    break;
                    default:
                        // other details
                        String[] param = line.split (":");
                        if (param[0].compareTo ("Host") == 0)
                            headerTable += tableRow (param[0], param[1] + ":" + param[2]);
                        else
                            headerTable += tableRow (param[0], param[1]);
                    break;
                }
                currLine ++;
                line = br.readLine ();
            }
        }
        catch (IOException ioe)
        {
            System.out.println ("Error!");
        }
        finally
        {
            try
            {
                // construct html page
                String html = pageStart + headerTable + pageEnd;
                // write said string to browser
                clientSocket.getOutputStream ().write (html.getBytes ("UTF-8"));
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace ();
            }
        }
    }

    // convenience method for writing table rows
    private String tableRow (String header, String value)
    {
        String tableRow = "\n<tr>\n\t<td>" + header + "</td>\n\t<td>" + value + "</td>\n</tr>";
        return tableRow;
    }

    // checks if a file is accessed or not. if a file is accessed, it will check if file exists. if file does not exist, issue 404. else, issue 200. if no file is accessed, issue 200.
    private void checkIfFileExists (String path)
    {
        if (!path.isEmpty ())
        {
            // Files needed
            headerTable += tableRow ("Files needed", path);
            // check if file exists
            File file = new File (path);
            if (!file.exists ())
            {
                headerTable += tableRow ("Status", "404 Not Found");
            }
            else
            {
                // put file path
                headerTable += tableRow ("File Path", file.getAbsolutePath ());
                headerTable += tableRow ("Status", "200 OK");
            }
        }
        else
        {
            // if there are no files to be accessed
            headerTable += tableRow ("Status", "200 OK");
        }
    }
}
