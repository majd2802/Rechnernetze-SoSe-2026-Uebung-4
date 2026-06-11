import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// =========================================================
// CLIENT HANDLER
// =========================================================

class ClientHandler {

    String name;
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    ClientHandler(
            String name,
            Socket socket,
            BufferedReader in,
            PrintWriter out
    ) {
        this.name = name;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }
}

// =========================================================
// DICE INVITATION
// =========================================================

class DiceInvitation {

    String inviter;
    String invited;

    DiceInvitation(String inviter, String invited) {
        this.inviter = inviter;
        this.invited = invited;
    }
}

// =========================================================
// TCP SERVER
// =========================================================

public class TcpServer {

    private static final Map<String, ClientHandler> clients =
            new ConcurrentHashMap<>();

    /*
     * Key = invited client name
     * Value = invitation information
     */
    private static final Map<String, DiceInvitation> pendingDiceInvitations =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {
        start(5000);
    }

    public static void start(int port) {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("TCP Server läuft auf Port " + port);

            while (true) {

                Socket socket = serverSocket.accept();

                new Thread(() -> handle(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // CLIENT THREAD
    // =====================================================

    private static void handle(Socket socket) {

        String name = null;

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(),
                        true
                )
        ) {

            // =================================================
            // REGISTRIERUNG
            // =================================================

            while (true) {

                out.println("register <name>");

                String reg = in.readLine();

                if (reg == null) {
                    socket.close();
                    return;
                }

                String[] parts = reg.trim().split("\\s+", 2);

                if (parts.length != 2 ||
                        !parts[0].equalsIgnoreCase("register")) {

                    out.println("Erwartet: register <name>");
                    continue;
                }

                name = parts[1].trim();

                if (name.isEmpty()) {
                    out.println("Name darf nicht leer sein.");
                    continue;
                }

                if (clients.containsKey(name)) {
                    out.println("Name bereits vergeben!");
                    continue;
                }

                break;
            }

            // =================================================
            // CLIENT REGISTRIEREN
            // =================================================

            ClientHandler handler =
                    new ClientHandler(name, socket, in, out);

            clients.put(name, handler);

            System.out.println(name + " connected");

            out.println("Registriert als " + name);
            out.println("Befehle:");
            out.println("send <Client> <Nachricht>");
            out.println("clientlist");
            out.println("sendall <Nachricht>");
            out.println("dice invite <Client>");
            out.println("dice join");
            out.println("dice decline");

            broadcast("SERVER: " + name + " ist verbunden.");

            // =================================================
            // MESSAGE LOOP
            // =================================================

            String msg;

            while ((msg = in.readLine()) != null) {

                msg = msg.trim();

                if (msg.isEmpty()) {
                    continue;
                }

                System.out.println(name + ": " + msg);

                // =============================================
                // SEND
                // send <Client> <Nachricht>
                // =============================================

                if (msg.startsWith("send ")) {
                    handleSend(name, msg);
                }

                // =============================================
                // CLIENTLIST
                // =============================================

                else if (msg.equalsIgnoreCase("clientlist")) {
                    sendClientList(name);
                }

                // =============================================
                // SENDALL
                // sendall <Nachricht>
                // =============================================

                else if (msg.startsWith("sendall ")) {
                    String text = msg.substring("sendall ".length());
                    broadcast("ALL " + name + ": " + text);
                }

                // =============================================
                // DICE INVITE
                // dice invite <Client>
                // =============================================

                else if (msg.startsWith("dice invite ")) {
                    handleDiceInvite(name, msg);
                }

                // =============================================
                // DICE JOIN
                // =============================================

                else if (msg.equalsIgnoreCase("dice join")) {
                    handleDiceJoin(name);
                }

                // =============================================
                // DICE DECLINE
                // =============================================

                else if (msg.equalsIgnoreCase("dice decline")) {
                    handleDiceDecline(name);
                }

                else {
                    clients.get(name).out.println("Unbekannter Befehl.");
                    clients.get(name).out.println("Befehle:");
                    clients.get(name).out.println("send <Client> <Nachricht>");
                    clients.get(name).out.println("clientlist");
                    clients.get(name).out.println("sendall <Nachricht>");
                    clients.get(name).out.println("dice invite <Client>");
                    clients.get(name).out.println("dice join");
                    clients.get(name).out.println("dice decline");
                }
            }

        } catch (Exception e) {

            System.out.println(name + " disconnected");

        } finally {

            // =================================================
            // DISCONNECT
            // =================================================

            if (name != null) {

                clients.remove(name);
                abortDiceGamesForClient(name);

                System.out.println("Removed: " + name);

                broadcast("SERVER: " + name + " hat die Verbindung getrennt.");
            }

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    // =====================================================
    // SEND TO ONE CLIENT
    // =====================================================

    private static void handleSend(String sender, String msg) {

        String[] parts = msg.split("\\s+", 3);

        if (parts.length != 3) {
            clients.get(sender).out.println("Usage: send <Client> <Nachricht>");
            return;
        }

        String target = parts[1];
        String text = parts[2];

        ClientHandler targetClient = clients.get(target);

        if (targetClient == null) {
            clients.get(sender).out.println("Client nicht gefunden: " + target);
            return;
        }

        targetClient.out.println(sender + ": " + text);
    }

    // =====================================================
    // CLIENTLIST
    // =====================================================

    private static void sendClientList(String requester) {

        ClientHandler client = clients.get(requester);

        if (client == null) {
            return;
        }

        if (clients.isEmpty()) {
            client.out.println("Keine Clients verbunden.");
            return;
        }

        StringJoiner joiner = new StringJoiner(", ");

        for (String name : clients.keySet()) {
            joiner.add(name);
        }

        client.out.println("Verbundene Clients: " + joiner);
    }

    // =====================================================
    // BROADCAST / SENDALL
    // =====================================================

    private static void broadcast(String message) {

        for (ClientHandler client : clients.values()) {
            client.out.println(message);
        }
    }

    // =====================================================
    // DICE INVITE
    // =====================================================

    private static void handleDiceInvite(String inviter, String msg) {

        String[] parts = msg.split("\\s+", 3);

        if (parts.length != 3) {
            clients.get(inviter).out.println("Usage: dice invite <Client>");
            return;
        }

        String invited = parts[2];

        if (inviter.equals(invited)) {
            clients.get(inviter).out.println("Du kannst dich nicht selbst einladen.");
            return;
        }

        if (!clients.containsKey(invited)) {
            clients.get(inviter).out.println("Client nicht gefunden: " + invited);
            return;
        }

        if (hasPendingDiceGame(inviter)) {
            clients.get(inviter).out.println("Du hast bereits eine offene Würfelspiel-Einladung.");
            return;
        }

        if (hasPendingDiceGame(invited)) {
            clients.get(inviter).out.println(invited + " hat bereits eine offene Würfelspiel-Einladung.");
            return;
        }

        DiceInvitation invitation = new DiceInvitation(inviter, invited);

        pendingDiceInvitations.put(invited, invitation);

        clients.get(inviter).out.println("Einladung an " + invited + " gesendet.");

        clients.get(invited).out.println(
                "DICE: " + inviter + " lädt dich zu einem Würfelspiel ein."
        );
        clients.get(invited).out.println(
                "Antworte mit: dice join oder dice decline"
        );
    }

    // =====================================================
    // DICE JOIN
    // =====================================================

    private static void handleDiceJoin(String invited) {

        DiceInvitation invitation = pendingDiceInvitations.remove(invited);

        if (invitation == null) {
            clients.get(invited).out.println("Keine offene Würfelspiel-Einladung vorhanden.");
            return;
        }

        String inviter = invitation.inviter;

        ClientHandler inviterClient = clients.get(inviter);
        ClientHandler invitedClient = clients.get(invited);

        if (inviterClient == null || invitedClient == null) {
            if (invitedClient != null) {
                invitedClient.out.println("Spiel abgebrochen, weil ein Client nicht mehr verbunden ist.");
            }
            return;
        }

        int inviterRoll1 = rollDice();
        int inviterRoll2 = rollDice();

        int invitedRoll1 = rollDice();
        int invitedRoll2 = rollDice();

        int inviterSum = inviterRoll1 + inviterRoll2;
        int invitedSum = invitedRoll1 + invitedRoll2;

        String resultForInviter;
        String resultForInvited;

        if (inviterSum > invitedSum) {
            resultForInviter = "Gewonnen";
            resultForInvited = "Verloren";
        } else if (inviterSum < invitedSum) {
            resultForInviter = "Verloren";
            resultForInvited = "Gewonnen";
        } else {
            resultForInviter = "Unentschieden";
            resultForInvited = "Unentschieden";
        }

        inviterClient.out.println("DICE: " + invited + " hat die Einladung angenommen.");
        invitedClient.out.println("DICE: Du hast die Einladung von " + inviter + " angenommen.");

        inviterClient.out.println(formatDiceResult(
                inviter,
                invited,
                inviterRoll1,
                inviterRoll2,
                invitedRoll1,
                invitedRoll2,
                inviterSum,
                invitedSum,
                resultForInviter
        ));

        invitedClient.out.println(formatDiceResult(
                inviter,
                invited,
                inviterRoll1,
                inviterRoll2,
                invitedRoll1,
                invitedRoll2,
                inviterSum,
                invitedSum,
                resultForInvited
        ));
    }

    // =====================================================
    // DICE DECLINE
    // =====================================================

    private static void handleDiceDecline(String invited) {

        DiceInvitation invitation = pendingDiceInvitations.remove(invited);

        if (invitation == null) {
            clients.get(invited).out.println("Keine offene Würfelspiel-Einladung vorhanden.");
            return;
        }

        String inviter = invitation.inviter;

        ClientHandler inviterClient = clients.get(inviter);
        ClientHandler invitedClient = clients.get(invited);

        if (invitedClient != null) {
            invitedClient.out.println("DICE: Du hast die Einladung abgelehnt.");
        }

        if (inviterClient != null) {
            inviterClient.out.println("DICE: " + invited + " hat die Einladung abgelehnt. Spiel abgebrochen.");
        }
    }

    // =====================================================
    // DICE HELPERS
    // =====================================================

    private static int rollDice() {
        return ThreadLocalRandom.current().nextInt(1, 7);
    }

    private static boolean hasPendingDiceGame(String clientName) {

        if (pendingDiceInvitations.containsKey(clientName)) {
            return true;
        }

        for (DiceInvitation invitation : pendingDiceInvitations.values()) {
            if (invitation.inviter.equals(clientName)) {
                return true;
            }
        }

        return false;
    }

    private static String formatDiceResult(
            String inviter,
            String invited,
            int inviterRoll1,
            int inviterRoll2,
            int invitedRoll1,
            int invitedRoll2,
            int inviterSum,
            int invitedSum,
            String result
    ) {
        return ""
                + "DICE Ergebnis:\n"
                + inviter + " würfelt: " + inviterRoll1 + " und " + inviterRoll2
                + " = " + inviterSum + "\n"
                + invited + " würfelt: " + invitedRoll1 + " und " + invitedRoll2
                + " = " + invitedSum + "\n"
                + "Ergebnis aus deiner Sicht: " + result;
    }

    private static void abortDiceGamesForClient(String clientName) {

        DiceInvitation invitation = pendingDiceInvitations.remove(clientName);

        if (invitation != null) {
            ClientHandler inviterClient = clients.get(invitation.inviter);

            if (inviterClient != null) {
                inviterClient.out.println(
                        "DICE: Spiel abgebrochen, weil "
                                + clientName
                                + " nicht mehr verbunden ist."
                );
            }
        }

        for (Map.Entry<String, DiceInvitation> entry : pendingDiceInvitations.entrySet()) {

            DiceInvitation current = entry.getValue();

            if (current.inviter.equals(clientName)) {

                pendingDiceInvitations.remove(entry.getKey());

                ClientHandler invitedClient = clients.get(current.invited);

                if (invitedClient != null) {
                    invitedClient.out.println(
                            "DICE: Einladung abgebrochen, weil "
                                    + clientName
                                    + " nicht mehr verbunden ist."
                    );
                }
            }
        }
    }
}