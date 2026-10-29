package santexfr.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import santexfr.ClientDetectorExtra;
import santexfr.api.ClientDetectorAPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HastebinUtils {
    public static void uploadDump(org.bukkit.command.CommandSender sender) {
        ClientDetectorExtra.getServerImplementation().async().runNow(() -> {
            try {
                ClientDetectorAPI api = ClientDetectorExtra.getApi();
                StringBuilder sb = new StringBuilder();
                sb.append("=== ClientDetectorExtra Server Dump ===\n");
                sb.append("Total Online Players: ").append(Bukkit.getOnlinePlayers().size()).append("\n");
                sb.append("Global Stats:\n");

                Map<String, Integer> stats = api.getGlobalStats();
                for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                    sb.append(" - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }

                sb.append("\nDetailed Players List:\n");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    sb.append(" - Player: ").append(p.getName())
                            .append(" | UUID: ").append(p.getUniqueId())
                            .append(" | Brand: ").append(api.getClientBrand(p))
                            .append(" | Bedrock: ").append(api.isBedrock(p))
                            .append("\n");
                }

                // Utilisation de l'API pastes.dev (très stable pour les plugins Minecraft)
                HttpURLConnection conn = (HttpURLConnection) URI.create("https://api.pastes.dev/post").toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                conn.setRequestProperty("User-Agent", "ClientDetectorExtra-Plugin");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                }

                String url = getPasteUrl(conn);

                ClientDetectorExtra.getServerImplementation().global().run(() ->
                        sender.sendMessage(ClientDetectorExtra.getInstance().getPrefix() + "§aDump généré avec succès : §b" + url)
                );

            } catch (Exception e) {
                ClientDetectorExtra.getServerImplementation().global().run(() ->
                        sender.sendMessage(ClientDetectorExtra.getInstance().getPrefix() + "§cErreur lors de la génération du dump : " + e.getMessage())
                );
            }
        });
    }

    private static @NonNull String getPasteUrl(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 201 && conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP Code " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        // Exemple de réponse JSON : {"key":"abc123xyz"}
        String raw = response.toString();
        String key = raw.contains("\"key\":\"") ? raw.split("\"key\":\"")[1].split("\"")[0] : "";

        if (key.isEmpty()) {
            throw new RuntimeException("Impossible d'extraire la clé du paste.");
        }

        return "https://pastes.dev/" + key;
    }
}