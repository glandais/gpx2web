package io.github.glandais.gpx.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** A raw HTTP server that accepts connections and never completes a response. */
public final class StallingServer implements AutoCloseable {

    public enum Mode {
        /** Read the request, then send nothing. */
        NO_RESPONSE,
        /** Send the 200 headers and a first chunk of the announced body, then nothing. */
        HEADERS_THEN_STALL
    }

    private final ServerSocket socket;
    private final List<Socket> clients = new CopyOnWriteArrayList<>();
    private final Thread acceptor;

    public StallingServer(Mode mode) throws IOException {
        socket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        acceptor = new Thread(() -> acceptLoop(mode), "stalling-server");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    public int port() {
        return socket.getLocalPort();
    }

    private void acceptLoop(Mode mode) {
        while (!socket.isClosed()) {
            try {
                Socket client = socket.accept();
                clients.add(client);
                readRequestHeaders(client.getInputStream());
                if (mode == Mode.HEADERS_THEN_STALL) {
                    OutputStream out = client.getOutputStream();
                    out.write(("HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nContent-Length: 100000\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
                    out.write(new byte[1000]);
                    out.flush();
                }
                // Keep the connection open and silent until close()
            } catch (SocketException e) {
                return;
            } catch (IOException e) {
                // next connection
            }
        }
    }

    private static void readRequestHeaders(InputStream in) throws IOException {
        byte[] end = "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
        int matched = 0;
        while (matched < end.length) {
            int b = in.read();
            if (b < 0) {
                return;
            }
            matched = b == end[matched] ? matched + 1 : (b == end[0] ? 1 : 0);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        for (Socket client : clients) {
            client.close();
        }
    }
}
