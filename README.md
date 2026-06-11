# Aufgabe 1: Protokoll-Header

## Vorgehen in Wireshark

Ich habe Wireshark auf meiner aktiven WLAN-Schnittstelle verwendet.
Für den UDP-Verkehr habe ich eine DNS-Anfrage mit folgendem Befehl erzeugt:

```text
nslookup uni-trier.de 8.8.8.8
```

Danach habe ich in Wireshark den Filter verwendet:

```text
dns && udp && ip
```

Für den TCP-Verkehr habe ich den Filter verwendet:

```text
tcp && ip
```

Anschließend habe ich jeweils ein passendes IPv4-, UDP- und TCP-Paket ausgewählt und die Header-Felder aus Wireshark den passenden Kategorien zugeordnet.

---

## 1. IPv4-Header

Ausgewähltes Paket: **Paket Nr. 477**

Dieses Paket ist ein IPv4-Paket, das ein UDP-Segment enthält. Es handelt sich um eine DNS-Anfrage von meinem Rechner an den DNS-Server `8.8.8.8`.

| IPv4-Header-Feld                                | Wert in Wireshark | Erklärung                                                                            |
| ----------------------------------------------- | ----------------: | ------------------------------------------------------------------------------------ |
| Version                                         |                 4 | Das Paket verwendet IPv4.                                                            |
| Header Length                                   |          20 bytes | Der IPv4-Header hat die normale Mindestgröße. Es sind keine IPv4-Optionen vorhanden. |
| Differentiated Services Field / Type of Service |              0x00 | Es wird keine besondere Dienstklasse verwendet.                                      |
| Total Length                                    |          66 bytes | Gesamtlänge des IPv4-Pakets, also IPv4-Header plus Nutzdaten.                        |
| Identification                                  |    0x3461 / 13409 | Dient dazu, Fragmente desselben ursprünglichen Datagramms zu erkennen.               |
| Flags                                           |               0x0 | Es ist kein besonderes Fragmentierungs-Flag gesetzt.                                 |
| Fragment Offset                                 |                 0 | Das Paket ist kein späteres Fragment.                                                |
| Time To Live                                    |               128 | Maximale Anzahl an Router-Hops, bevor das Paket verworfen wird.                      |
| Protocol                                        |          UDP (17) | Das IPv4-Paket enthält als Nutzdaten ein UDP-Segment.                                |
| Header Checksum                                 |            0x3434 | Prüfsumme für den IPv4-Header.                                                       |
| Source Address                                  |      192.168.1.94 | IP-Adresse meines Rechners.                                                          |
| Destination Address                             |           8.8.8.8 | IP-Adresse des DNS-Servers.                                                          |

Der IPv4-Header gehört zur Vermittlungs-/Netzwerkschicht. Er enthält Informationen, die benötigt werden, um ein Paket vom Quellhost zum Zielhost zu transportieren. Besonders wichtig sind Quelladresse, Zieladresse, TTL, Protokollfeld, Fragmentierungsfelder und Header-Prüfsumme.

---

## 2. UDP-Header

Ausgewähltes Paket: **Paket Nr. 477**

Dieses UDP-Paket enthält eine DNS-Anfrage.

| UDP-Header-Feld  | Wert in Wireshark | Erklärung                                 |
| ---------------- | ----------------: | ----------------------------------------- |
| Source Port      |             63607 | Temporärer Client-Port meines Rechners.   |
| Destination Port |                53 | Zielport für DNS.                         |
| Length           |          46 bytes | Länge des UDP-Headers plus UDP-Nutzdaten. |
| Checksum         |            0x1ba1 | Prüfsumme zur Fehlererkennung.            |
| UDP payload      |          38 bytes | Die Nutzdaten enthalten die DNS-Anfrage.  |

Der UDP-Header gehört zur Transportschicht. Im Vergleich zum IPv4-Header ist der UDP-Header sehr einfach aufgebaut. Er enthält nur Quellport, Zielport, Länge und Prüfsumme. In diesem Beispiel wird UDP benutzt, um eine DNS-Anfrage an Port 53 zu übertragen.

UDP ist verbindungslos. Das bedeutet, dass vor dem Senden keine Verbindung aufgebaut wird. Außerdem enthält UDP keine Sequenznummern und keine Bestätigungsnummern wie TCP.

---

## 3. TCP-Header

Ausgewähltes Paket: **Paket Nr. 475**

Dieses Paket ist ein IPv4-Paket, das ein TCP-Segment enthält. Es handelt sich um ein TCP-ACK-Paket von einem Server zurück an meinen Rechner.

| TCP-Header-Feld        |        Wert in Wireshark | Erklärung                                                                            |
| ---------------------- | -----------------------: | ------------------------------------------------------------------------------------ |
| Source Port            |                      443 | Quellport des Servers. Port 443 wird typischerweise für HTTPS verwendet.             |
| Destination Port       |                    57506 | Temporärer Client-Port meines Rechners.                                              |
| Sequence Number        |                     1087 | Wird von TCP verwendet, um den Bytestrom zu ordnen.                                  |
| Acknowledgment Number  |                      544 | Bestätigt, dass Daten bis zu dieser Stelle empfangen wurden.                         |
| Header Length          |                 32 bytes | Größe des TCP-Headers. Er ist größer als 20 Bytes, weil TCP-Optionen vorhanden sind. |
| Flags                  |                      ACK | Dieses Paket bestätigt empfangene TCP-Daten.                                         |
| Window                 |                     9388 | Empfangsfenster für die TCP-Flusskontrolle.                                          |
| Calculated Window Size |                     9388 | Effektive Fenstergröße, die Wireshark berechnet.                                     |
| Checksum               |                   0x87a2 | TCP-Prüfsumme zur Fehlererkennung.                                                   |
| Urgent Pointer         |                        0 | Nicht verwendet, da das URG-Flag nicht gesetzt ist.                                  |
| Options                | 12 bytes: NOP, NOP, SACK | TCP-Optionen sind vorhanden.                                                         |
| TCP Segment Len        |                        0 | Dieses TCP-Paket enthält keine Anwendungsdaten, sondern ist nur ein ACK-Paket.       |

Der TCP-Header gehört ebenfalls zur Transportschicht. TCP ist deutlich komplexer als UDP, weil TCP eine zuverlässige, geordnete Bytestrom-Kommunikation bereitstellt. Dafür verwendet TCP unter anderem Sequenznummern, Bestätigungsnummern, Flags, Fenstergröße und Optionen.

In diesem Paket ist das ACK-Flag gesetzt. Das bedeutet, dass dieses Paket den Empfang von Daten bestätigt. Da die Segmentlänge 0 ist, enthält dieses Paket selbst keine Anwendungsdaten.

---

## Vergleich IPv4, UDP und TCP

IPv4 gehört zur Netzwerkschicht und ist für die Adressierung und Weiterleitung von Paketen zwischen Hosts zuständig. Der IPv4-Header enthält deshalb unter anderem Quell-IP-Adresse, Ziel-IP-Adresse, TTL, Protokollfeld und Fragmentierungsinformationen.

UDP und TCP gehören zur Transportschicht. Beide verwenden Ports, um Anwendungen beziehungsweise Prozesse auf einem Host zu adressieren.

UDP hat einen sehr kleinen Header mit nur wenigen Feldern: Quellport, Zielport, Länge und Prüfsumme. UDP ist verbindungslos und bietet selbst keine zuverlässige Übertragung.

TCP hat einen größeren und komplexeren Header. TCP verwendet Sequenznummern, Bestätigungsnummern, Flags, Fenstergröße und Optionen. Dadurch kann TCP eine zuverlässige, geordnete Kommunikation als Bytestrom ermöglichen.



# Aufgabe 2: CIDR

## a) Beschreibung von 103.161.122.83/18

Die Schreibweise `103.161.122.83/18` ist eine CIDR-Schreibweise.

`103.161.122.83` ist die konkrete IPv4-Adresse eines Hosts.

Die Zahl `/18` gibt an, dass die ersten 18 Bits der Adresse der Netzanteil sind. Die restlichen Bits sind der Hostanteil.

Eine IPv4-Adresse hat insgesamt 32 Bits.

Also gilt:

* Netzanteil: 18 Bits
* Hostanteil: 32 - 18 = 14 Bits

Aus `/18` ergibt sich die Subnetzmaske:

```text
11111111.11111111.11000000.00000000
```

In Dezimalschreibweise ist das:

```text
255.255.192.0
```

Um die Netzwerkadresse zu bestimmen, setzt man alle Hostbits auf 0.

Um die Broadcastadresse zu bestimmen, setzt man alle Hostbits auf 1.

Für `103.161.122.83/18` ergibt sich:

| Kategorie        |            Wert |
| ---------------- | --------------: |
| IP-Adresse       |  103.161.122.83 |
| Präfixlänge      |             /18 |
| Subnetzmaske     |   255.255.192.0 |
| Netzwerkadresse  |    103.161.64.0 |
| Broadcastadresse | 103.161.127.255 |

Kurze Erklärung:

Bei `/18` sind die ersten zwei Oktette vollständig Netzanteil:

```text
103.161
```

Im dritten Oktett gehören nur die ersten 2 Bits zum Netzanteil, weil 16 Bits schon durch die ersten zwei Oktette verbraucht sind und noch 2 Bits fehlen.

Die Blockgröße im dritten Oktett ist:

```text
256 - 192 = 64
```

Die möglichen Netzbereiche im dritten Oktett sind also:

```text
0-63
64-127
128-191
192-255
```

Die IP-Adresse `103.161.122.83` liegt im Bereich `64-127`.

Deshalb ist die Netzwerkadresse:

```text
103.161.64.0
```

und die Broadcastadresse:

```text
103.161.127.255
```

---

## b) Subnetzmaske, Broadcastadresse und Netzwerkadresse

### (i) 172.16.45.200/20

Bei `/20` sind 20 Bits Netzanteil und 12 Bits Hostanteil.

Die Subnetzmaske ist:

```text
255.255.240.0
```

Die Blockgröße im dritten Oktett ist:

```text
256 - 240 = 16
```

Die möglichen Bereiche im dritten Oktett sind:

```text
0-15
16-31
32-47
48-63
...
```

Die IP-Adresse `172.16.45.200` liegt im Bereich `32-47`.

| Kategorie        |             Wert |
| ---------------- | ---------------: |
| IP-Adresse       | 172.16.45.200/20 |
| Subnetzmaske     |    255.255.240.0 |
| Netzwerkadresse  |      172.16.32.0 |
| Broadcastadresse |    172.16.47.255 |

---

### (ii) 192.168.14.77/26

Bei `/26` sind 26 Bits Netzanteil und 6 Bits Hostanteil.

Die Subnetzmaske ist:

```text
255.255.255.192
```

Die Blockgröße im vierten Oktett ist:

```text
256 - 192 = 64
```

Die möglichen Bereiche im vierten Oktett sind:

```text
0-63
64-127
128-191
192-255
```

Die IP-Adresse `192.168.14.77` liegt im Bereich `64-127`.

| Kategorie        |             Wert |
| ---------------- | ---------------: |
| IP-Adresse       | 192.168.14.77/26 |
| Subnetzmaske     |  255.255.255.192 |
| Netzwerkadresse  |    192.168.14.64 |
| Broadcastadresse |   192.168.14.127 |

---

### (iii) 10.55.201.19/13

Bei `/13` sind 13 Bits Netzanteil und 19 Bits Hostanteil.

Die Subnetzmaske ist:

```text
255.248.0.0
```

Die Blockgröße im zweiten Oktett ist:

```text
256 - 248 = 8
```

Die möglichen Bereiche im zweiten Oktett sind:

```text
0-7
8-15
16-23
24-31
32-39
40-47
48-55
56-63
...
```

Die IP-Adresse `10.55.201.19` liegt im Bereich `48-55`.

| Kategorie        |            Wert |
| ---------------- | --------------: |
| IP-Adresse       | 10.55.201.19/13 |
| Subnetzmaske     |     255.248.0.0 |
| Netzwerkadresse  |       10.48.0.0 |
| Broadcastadresse |   10.55.255.255 |

---

## c) Liegt 103.161.122.83/18 im selben Netz wie 103.161.193.83/18?

Nein, die beiden IP-Adressen liegen nicht im selben Netz.

Für beide Adressen gilt die Präfixlänge `/18`.

Die Subnetzmaske ist also bei beiden:

```text
255.255.192.0
```

Bei `/18` ist die Blockgröße im dritten Oktett:

```text
256 - 192 = 64
```

Die möglichen Netzbereiche im dritten Oktett sind:

```text
0-63
64-127
128-191
192-255
```

Die erste Adresse ist:

```text
103.161.122.83/18
```

Das dritte Oktett ist `122`.

`122` liegt im Bereich `64-127`.

Also gehört diese Adresse zum Netz:

```text
103.161.64.0/18
```

Die zweite Adresse ist:

```text
103.161.193.83/18
```

Das dritte Oktett ist `193`.

`193` liegt im Bereich `192-255`.

Also gehört diese Adresse zum Netz:

```text
103.161.192.0/18
```

Da die Netzwerkadressen unterschiedlich sind, liegen die beiden IP-Adressen nicht im selben Netz.

| IP-Adresse        |  Netzwerkadresse |
| ----------------- | ---------------: |
| 103.161.122.83/18 |  103.161.64.0/18 |
| 103.161.193.83/18 | 103.161.192.0/18 |

Ergebnis:

```text
Nein, sie liegen nicht im selben Netz.
```


# Aufgabe 4: Kommunikation zwischen Implementationen

## Ausgangssituation

Ein direkter Test mit einer fremden Implementierung von Kommilitoninnen oder Kommilitonen war mir nicht möglich, weil mir keine andere Implementierung zum Testen zur Verfügung stand.

Ich habe deshalb meine eigene Implementierung lokal getestet und zusätzlich dokumentiert, welche Probleme bei der Kommunikation mit anderen Implementierungen wahrscheinlich auftreten können und wie man diese Probleme lösen könnte.

---

## Lokaler Test meiner eigenen Implementierung

### UDP-Test

Für UDP habe ich zwei Clients lokal gestartet, aber mit unterschiedlichen Ports.

Beispiel:

```text
java UdpClient Alice 6001
java UdpClient Bob 6002
```

Danach wurden die Clients gegenseitig registriert:

```text
register Bob 127.0.0.1 6002
register Alice 127.0.0.1 6001
```

Anschließend konnten Nachrichten gesendet werden:

```text
send Bob Hallo Bob
sendall Hallo an alle gespeicherten Clients
clientlist
```

Die Kommunikation zwischen den lokalen UDP-Clients funktioniert, wenn IP-Adresse und Port korrekt sind.

---

### TCP-Test

Für TCP habe ich zuerst den Server gestartet:

```text
java TcpServer
```

Danach habe ich zwei Clients gestartet:

```text
java TcpClient 127.0.0.1 5000
java TcpClient 127.0.0.1 5000
```

Die Clients wurden mit Namen registriert:

```text
register Alice
register Bob
```

Danach wurden die Befehle getestet:

```text
clientlist
send Bob Hallo Bob
sendall Hallo an alle
dice invite Bob
dice join
dice decline
```

Die Kommunikation zwischen den lokalen TCP-Clients funktioniert, wenn beide Clients mit demselben Server verbunden sind und die erwarteten Befehle verwenden.

---

## Mögliche Probleme bei der Kommunikation mit anderen Implementierungen

Auch wenn UDP und TCP technisch funktionieren, können unterschiedliche Implementierungen trotzdem nicht korrekt miteinander kommunizieren. Der Grund ist meistens nicht UDP oder TCP selbst, sondern das selbst definierte Chat-Protokoll auf Anwendungsebene.

### Problem 1: Unterschiedliche Befehle

Meine TCP-Implementierung erwartet zum Beispiel:

```text
send <Client> <Nachricht>
```

Eine andere Implementierung könnte aber vielleicht Folgendes verwenden:

```text
message <Client> <Nachricht>
```

oder:

```text
msg <Client> <Nachricht>
```

Dann kommt die Nachricht zwar beim Server an, aber der Server erkennt den Befehl nicht.

Mögliche Lösung:

Vorher ein gemeinsames Befehlsformat festlegen, zum Beispiel:

```text
register <Name>
send <Client> <Nachricht>
clientlist
sendall <Nachricht>
dice invite <Client>
dice join
dice decline
```

---

### Problem 2: Unterschiedliches Nachrichtenformat

Meine UDP-Implementierung verschickt Nachrichten in der Form:

```text
<Name>: <Nachricht>
```

Eine andere Implementierung könnte aber ein anderes Format erwarten, zum Beispiel:

```text
SEND|Alice|Bob|Hallo
```

oder JSON:

```json
{
  "type": "send",
  "from": "Alice",
  "to": "Bob",
  "message": "Hallo"
}
```

Wenn beide Programme unterschiedliche Formate verwenden, kann die Nachricht eventuell nicht richtig verarbeitet werden.

Mögliche Lösung:

Ein gemeinsames Nachrichtenformat definieren. Ein strukturiertes Format wie JSON wäre besonders gut, weil die Bedeutung der einzelnen Felder klar ist.

---

### Problem 3: Unterschiedliche Registrierung

Mein TCP-Server erwartet nach dem Verbinden:

```text
register <name>
```

Ein anderer TCP-Client könnte aber direkt nur den Namen senden:

```text
Alice
```

Dann würde mein Server die Registrierung nicht akzeptieren.

Mögliche Lösung:

Die Registrierung muss eindeutig spezifiziert werden. Zum Beispiel immer:

```text
register <name>
```

---

### Problem 4: Falsche IP-Adresse oder falscher Port bei UDP

UDP verwendet keinen zentralen Server. Ein Client sendet direkt an eine IP-Adresse und einen Port. Wenn die IP-Adresse oder der Port falsch ist, kommt die Nachricht nicht an.

Mögliche Lösung:

Vor dem Test müssen IP-Adresse und Port ausgetauscht werden. Außerdem sollte man mit einfachen Testnachrichten beginnen.

---

### Problem 5: UDP garantiert keine Zustellung

UDP ist verbindungslos. Es gibt keine automatische Bestätigung, ob eine Nachricht angekommen ist. Pakete können verloren gehen oder in anderer Reihenfolge ankommen.

Mögliche Lösung:

Falls zuverlässige Zustellung benötigt wird, könnte man auf Anwendungsebene Bestätigungen einbauen, zum Beispiel:

```text
ACK <Nachrichten-ID>
```

Alternativ kann man TCP verwenden.

---

### Problem 6: Würfelspiel nur bei gleicher Serverlogik möglich

Das Würfelspiel funktioniert nur, wenn der TCP-Server die Befehle kennt:

```text
dice invite <Client>
dice join
dice decline
```

Wenn ein anderer Client oder Server andere Befehle verwendet, funktioniert das Spiel nicht.

Mögliche Lösung:

Das Protokoll für das Würfelspiel muss genau festgelegt werden. Dazu gehören Einladung, Annahme, Ablehnung, Würfelwürfe und Ergebnisnachrichten.

---

## Verbesserungsvorschlag: Gemeinsames Protokoll

Damit unterschiedliche Implementierungen besser miteinander kommunizieren können, sollte man vor der Implementierung ein gemeinsames Protokoll festlegen.

Eine einfache textbasierte Variante wäre:

```text
REGISTER <Name>
SEND <Empfänger> <Nachricht>
SENDALL <Nachricht>
CLIENTLIST
DICE_INVITE <Empfänger>
DICE_JOIN
DICE_DECLINE
```

Eine bessere Variante wäre ein strukturiertes JSON-Format, zum Beispiel:

```json
{
  "type": "send",
  "from": "Alice",
  "to": "Bob",
  "message": "Hallo Bob"
}
```

Für `sendall` könnte man schreiben:

```json
{
  "type": "sendall",
  "from": "Alice",
  "message": "Hallo an alle"
}
```

Für das Würfelspiel könnte man schreiben:

```json
{
  "type": "dice_invite",
  "from": "Alice",
  "to": "Bob"
}
```

Dadurch wäre klarer, welche Art von Nachricht gesendet wird und welche Felder dazugehören.

---

## Fazit

Ein direkter Test mit einer fremden Implementierung war nicht möglich, weil mir keine andere Implementierung zur Verfügung stand. Meine eigene Implementierung wurde lokal mit mehreren Clients getestet.

Die wichtigsten Kompatibilitätsprobleme entstehen wahrscheinlich durch unterschiedliche Befehle, unterschiedliche Nachrichtenformate, unterschiedliche Registrierung und falsche IP-/Port-Angaben bei UDP.

Die beste Lösung wäre ein vorher festgelegtes gemeinsames Chat-Protokoll. Besonders sinnvoll wäre ein strukturiertes Format wie JSON, weil dadurch die Bedeutung der einzelnen Nachrichtenfelder eindeutig ist.

