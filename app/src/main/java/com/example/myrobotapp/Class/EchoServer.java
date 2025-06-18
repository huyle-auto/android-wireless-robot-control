package com.example.myrobotapp.Class;

import java.io.*;
import java.net.*;

public class EchoServer {
    private ServerSocket serverSocket;

    // Khởi tạo server trước, nếu thành công thì bắt đầu chờ các kết nối từ client
    public void start(int port) throws IOException{
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e) {

        }

        while (true) {
            // accept() nếu thực thi thành công sẽ mở một clientSocket tới server (port mới của server vì là nhiều client)
            // nếu không kết nối được thì bị connection refused Exception.
            // !! serverSocket.accept() trả về một socket chính là client Socket
            // Gọi start() (method của Thread) sẽ tạo một Thread mới và chạy luôn lệnh run(). Nếu gọi run() sẽ gọi đè lên thread cũ !!
            new EchoClientHandler(serverSocket.accept()).start();
        }
    }

    public void stop() throws IOException{
        serverSocket.close();
    }

    private static class EchoClientHandler extends Thread{
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        // Constructor
        // Ở trên serverSocket.accept() trả về một object socket nên sẽ gán vào property này
        public EchoClientHandler (Socket socket){
            this.clientSocket = socket;
            System.out.println("A client connected to this server !");
        }

        // Chạy Threads
        @Override
        public void run(){
            try {
                // Định nghĩa Input/Output Stream của client với PrintWriter và BufferedReader
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }
            catch (IOException e) {

            }

            // Liên tục đọc chuỗi từ client tới, kiểm tra xem có phải null không, nếu không thì kiểm tra xem có phải ký tự "." không, nếu
            // không phải thì in ra, nếu là ký tự "." thì thoát vòng lặp while
            String inputLine;
            System.out.println("Waiting for Heartbeat mechanism message from client...");
            while (true) {
                try {
                    inputLine = in.readLine();
                    if (inputLine == null){
                        break;
                    }
                    if ("ping".equals(inputLine)){
                        out.println("pong");
                        System.out.println("Sent alive notif to client !");
                    }
                    if (!"ping".equals(inputLine)){
                        System.out.println("No message from client. Probably client disconnected");
                    }

                }
                catch (IOException e) {

                }
            }
        }
    }
}
