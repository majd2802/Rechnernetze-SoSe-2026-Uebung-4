import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpClient {

    private static final Map<String, InetSocketAddress> recipients =
            new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Usage: java UdpClient <name> <port>");
            return;
        }

        String ownName = args[0];
        int ownPort = Integer.parseInt(args[1]);

        DatagramSocket socket = new DatagramSocket(ownPort);

        System.out.println("UDP gestartet:");
        System.out.println("Name: " + ownName);
        System.out.println("Port: " + ownPort);
        printHelp();

        // =====================================================
        // RECEIVER THREAD
        // =====================================================

        Thread receiver = new Thread(() -> {

            byte[] buffer = new byte[4096];

            while (true) {

                try {

                    DatagramPacket packet =
                            new DatagramPacket(buffer, buffer.length);

                    socket.receive(packet);

                    String message = new String(
                            packet.getData(),
                            0,
                            packet.getLength(),
                            StandardCharsets.UTF_8
                    );

                    System.out.println();
                    System.out.println("Empfangen von "
                            + packet.getAddress().getHostAddress()
                            + ":"
                            + packet.getPort());
                    System.out.println(message);
                    System.out.print("> ");

                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        });

        receiver.start();

        BufferedReader keyboard =
                new BufferedReader(new InputStreamReader(System.in));

        // =====================================================
        // INPUT LOOP
        // =====================================================

        while (true) {

            System.out.print("> ");

            String input = keyboard.readLine();

            if (input == null) {
                continue;
            }

            input = input.trim();

            // =================================================
            // REGISTER
            // register <Name> <IP-Adresse> <Port>
            // =================================================

            if (input.startsWith("register ")) {

                String[] parts = input.split("\\s+", 4);

                if (parts.length != 4) {
                    System.out.println("Usage: register <Name> <IP-Adresse> <Port>");
                    continue;
                }

                String name = parts[1];
                String ip = parts[2];
                int port = Integer.parseInt(parts[3]);

                InetSocketAddress address =
                        new InetSocketAddress(InetAddress.getByName(ip), port);

                recipients.put(name, address);

                System.out.println("Empfänger gespeichert: "
                        + name + " -> " + ip + ":" + port);
            }

            // =================================================
            // CLIENTLIST
            // =================================================

            else if (input.equalsIgnoreCase("clientlist")) {

                if (recipients.isEmpty()) {
                    System.out.println("Keine gespeicherten Empfänger.");
                } else {
                    System.out.println("Gespeicherte Empfänger:");

                    for (Map.Entry<String, InetSocketAddress> entry : recipients.entrySet()) {
                        InetSocketAddress address = entry.getValue();

                        System.out.println("- "
                                + entry.getKey()
                                + " -> "
                                + address.getAddress().getHostAddress()
                                + ":"
                                + address.getPort());
                    }
                }
            }

            // =================================================
            // SENDALL
            // sendall <Nachricht>
            // =================================================

            else if (input.startsWith("sendall ")) {

                String message = input.substring("sendall ".length());

                if (recipients.isEmpty()) {
                    System.out.println("Keine gespeicherten Empfänger.");
                    continue;
                }

                for (Map.Entry<String, InetSocketAddress> entry : recipients.entrySet()) {
                    sendMessage(socket, ownName, entry.getValue(), message);
                }

                System.out.println("Nachricht an alle gespeicherten Empfänger gesendet.");
            }

            // =================================================
            // SEND TO REGISTERED NAME
            // send <Name> <Nachricht>
            // =================================================

            else if (input.startsWith("send ")) {

                String[] parts = input.split("\\s+", 3);

                if (parts.length != 3) {
                    System.out.println("Usage: send <Name> <Nachricht>");
                    System.out.println("Oder: sendraw <IP-Adresse> <Port> <Nachricht>");
                    continue;
                }

                String targetName = parts[1];
                String message = parts[2];

                InetSocketAddress target = recipients.get(targetName);

                if (target == null) {
                    System.out.println("Empfänger nicht gefunden: " + targetName);
                    continue;
                }

                sendMessage(socket, ownName, target, message);
            }

            // =================================================
            // SENDRAW: old direct sending style
            // sendraw <ip> <port> <message>
            // =================================================

            else if (input.startsWith("sendraw ")) {

                String[] parts = input.split("\\s+", 4);

                if (parts.length != 4) {
                    System.out.println("Usage: sendraw <IP-Adresse> <Port> <Nachricht>");
                    continue;
                }

                String ip = parts[1];
                int port = Integer.parseInt(parts[2]);
                String message = parts[3];

                InetSocketAddress target =
                        new InetSocketAddress(InetAddress.getByName(ip), port);

                sendMessage(socket, ownName, target, message);
            }

            // =================================================
            // EXIT
            // =================================================

            else if (input.equalsIgnoreCase("exit")) {

                socket.close();
                System.exit(0);
            }

            // =================================================
            // HELP
            // =================================================

            else if (input.equalsIgnoreCase("help")) {
                printHelp();
            }

            else {
                System.out.println("Unbekannter Befehl.");
                printHelp();
            }
        }
    }

    private static void sendMessage(
            DatagramSocket socket,
            String ownName,
            InetSocketAddress target,
            String message
    ) throws Exception {

        String finalMessage = ownName + ": " + message;

        byte[] data = finalMessage.getBytes(StandardCharsets.UTF_8);

        DatagramPacket packet =
                new DatagramPacket(
                        data,
                        data.length,
                        target.getAddress(),
                        target.getPort()
                );

        socket.send(packet);
    }

    private static void printHelp() {
        System.out.println("Befehle:");
        System.out.println("register <Name> <IP-Adresse> <Port>");
        System.out.println("clientlist");
        System.out.println("send <Name> <Nachricht>");
        System.out.println("sendall <Nachricht>");
        System.out.println("sendraw <IP-Adresse> <Port> <Nachricht>");
        System.out.println("exit");
    }
}