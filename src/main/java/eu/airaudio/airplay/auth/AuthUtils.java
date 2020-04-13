package eu.airaudio.airplay.auth;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martin on 24.05.2017.
 */
public class AuthUtils {
    static byte[] concatByteArrays(byte[]... byteArrays) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (byte[] bytes : byteArrays) {
            byteArrayOutputStream.write(bytes);
        }
        return byteArrayOutputStream.toByteArray();
    }

    static byte[] createPList(Map<String, ? extends Object> properties) throws IOException {
        ByteArrayOutputStream plistOutputStream = new ByteArrayOutputStream();
        NSDictionary root = new NSDictionary();
        for (Map.Entry<String, ? extends Object> property : properties.entrySet()) {
            root.put(property.getKey(), property.getValue());
        }
        PropertyListParser.saveAsBinary(root, plistOutputStream);
        return plistOutputStream.toByteArray();
    }

    public static byte[] postData(Socket socket, String path, String contentType, byte[] data) throws IOException {
        DataOutputStream wr = new DataOutputStream(socket.getOutputStream());
        wr.writeBytes("POST " + path + " HTTP/1.0\r\n");
        wr.writeBytes("User-Agent: AirPlay/320.20\r\n");
        wr.writeBytes("Connection: keep-alive\r\n");

        if (data != null) {
            wr.writeBytes("Content-Length: " + data.length + "\r\n");
            wr.writeBytes("Content-Type: " + contentType + "\r\n");
        }
        wr.writeBytes("\r\n");

        if (data != null) {
            wr.write(data);
        }
        wr.flush();

        String line;

        Pattern statusPattern = Pattern.compile("HTTP[^ ]+ (\\d{3})");
        Pattern contentLengthPattern = Pattern.compile("Content-Length: (\\d+)");

        int contentLength = 0;
        int statusCode = 0;

        while ((line = AuthUtils.readLine(socket.getInputStream())) != null) {
            System.out.println(line);
            Matcher statusMatcher = statusPattern.matcher(line);
            if (statusMatcher.find()) {
                statusCode = Integer.parseInt(statusMatcher.group(1));
            }
            Matcher contentLengthMatcher = contentLengthPattern.matcher(line);
            if (contentLengthMatcher.find()) {
                contentLength = Integer.parseInt(contentLengthMatcher.group(1));
            }
            if (line.trim().isEmpty()) {
                break;
            }
        }

        if (statusCode != 200) {
            throw new IOException("Invalid status code " + statusCode);
        }

        try (ByteArrayOutputStream response = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[0xFFFF];

            for (int len; response.size() < contentLength && (len = socket.getInputStream().read(buffer)) != -1; ) {
                response.write(buffer, 0, len);
            }

            response.flush();

            return response.toByteArray();
        }
    }

    public static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != '\n' && c != -1; c = inputStream.read()) {
            byteArrayOutputStream.write(c);
        }
        if (c == -1 && byteArrayOutputStream.size() == 0) {
            return null;
        }
        String line = byteArrayOutputStream.toString("UTF-8");
        return line;
    }

    public static String randomString(final int length) {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(chars[rnd.nextInt(chars.length)]);

        return sb.toString();
    }

}
