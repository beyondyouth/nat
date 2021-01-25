/*
 * @Author: dong.zhili
 * @Date: 2021-01-25 14:34:51
 * @LastEditors: dong.zhili
 * @LastEditTime: 2021-01-25 15:18:24
 */
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class Relay {
    // 记录所有当前连接信息的集合
    static Set<Socket> connSet = new HashSet<>();

    public static void main(String[] args) throws IOException {
        // 定义一个ServerSocket监听在端口8888上
        ServerSocket ss = new ServerSocket(8888);
        new Thread(new MoniterTask()).start();
        System.out.printf("[%tc] ", new Date());
        System.out.println("启动中继服务....");
        while (true) {
            Socket s = ss.accept();
            // 每接收到一个Socket就添加到集合
            connSet.add(s);
            // 每接收到一个Socket就建立一个新的线程来处理它
            new Thread(new SocketTask(s)).start();

        }
    }
    /**
     * 用来监控Socket连接的
     */
    static class MoniterTask implements Runnable {
        public void run() {
            while (true) {
                for (Socket s : connSet) {
                    if(s.isClosed())
                        connSet.remove(s);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 用来处理Socket请求的
     */
    static class SocketTask implements Runnable {

        private Socket socket;

        public SocketTask(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                handleSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 跟客户端Socket进行通信
         * @throws Exception
         */
        private void handleSocket() throws Exception {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String temp;
            InetAddress clientaddr = socket.getInetAddress();
            System.out.printf("[%tc] ", new Date());
            System.out.println("客户端: " + clientaddr.getHostAddress() + ":" + socket.getPort() + " 已连接到服务器");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while ((temp=br.readLine()) != null) {
                System.out.printf("[%tc] ", new Date());
                System.out.println("客户端: "+temp);
                if(temp.equals("list")) {
                    // 接收到list命令则输出所有客户端连接信息
                    for (Socket s : connSet) {
                        String str = s.getInetAddress().getHostAddress() + ":" + s.getPort();
                        System.out.println(str);
                        bw.write(str+"\n");
                    }
                    bw.flush();
                }

            }
            bw.close();
            br.close();
            socket.close();
        }
    }
}
