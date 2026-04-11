package santexfr.utils;

import santexfr.ClientDetectorExtra;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    public static void send(String urlString, String content) {
        if(urlString == null || urlString.isEmpty()) return;
        ClientDetectorExtra.getServerImplementation().async().runNow(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) java.net.URI.create(urlString).toURL().openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "ClientDetectorExtra");
                connection.setDoOutput(true);

                String json = "{\"content\": \"" + content.replace("\"", "\\\"") + "\"}";
                try(OutputStream os = connection.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
                connection.getInputStream().close();
            } catch (Exception ignored) { }
        });
    }
}