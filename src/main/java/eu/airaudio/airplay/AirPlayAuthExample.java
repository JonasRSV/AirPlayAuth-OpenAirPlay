package eu.airaudio.airplay;

import eu.airaudio.airplay.auth.AirPlayAuth;
import eu.airaudio.airplay.auth.AuthUtils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Martin on 08.05.2017.
 */
public class AirPlayAuthExample {

    // Generated via {@code AirPlayAuth.generateNewAuthToken()}
    private static final String STORED_AUTH_TOKEN = "I1Y67NO1F5VFAVPG@302e020100300506032b657004220420eb92ab919f68cc716f7f85a609531c3de74f87c9f1c9007c35516b4f5ef1fa25";

    public static final String IP = "192.168.10.227";
    public static final int PORT = 7000;

    public static void main(String[] args) throws Exception {

        System.out.println("Used AuthKey: " + STORED_AUTH_TOKEN);
        AirPlayAuth airPlayAuth = new AirPlayAuth(new InetSocketAddress(IP, PORT), STORED_AUTH_TOKEN);
        Socket socket;
        try {
            socket = airPlayAuth.authenticate();
        } catch (Exception e) {
            System.out.println("Authentication failed - start pairing..");

            airPlayAuth.startPairing();

            System.out.println("Enter PIN:");
            Scanner scan = new Scanner(System.in);
            String pin = scan.nextLine().trim();

            airPlayAuth.doPairing(pin);

            socket = airPlayAuth.authenticate();
        }

        CmdLineParser cmd = new CmdLineParser();
        CmdLineParser.Option hostopt = cmd.addStringOption('h',"hostname");
        cmd.parse(args);

        String hostname = (String) cmd.getOptionValue(hostopt);
        AirPlay airplay;
        airplay = new AirPlay(hostname, socket);
        airplay.desktop();

        //String content = "Content-Location: http://techslides.com/demos/sample-videos/small.mp4\r\n" +
          //      "Start-Position: 0.0\r\n";

        //AuthUtils.postData(socket, "/play", "text/parameters", content.getBytes("UTF-8"));

        Thread.sleep(10000);

        socket.close();

    }
}
