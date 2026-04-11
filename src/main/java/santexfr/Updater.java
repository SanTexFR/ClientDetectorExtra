package santexfr;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Updater{
    //VARIABLES (STATICS)
    public static final@NotNull String USER="SanTexFR";
    public static final@NotNull String REPO="ClientDetectorExtra";

    //METHODS (STATICSà
    public static void checkForUpdates(){
        ClientDetectorExtra.getServerImplementation().async().runNow(()->{
            try{
                final String latestTag=getLatestTag();
                final String currentVersion=ClientDetectorExtra.getInstance().getPluginMeta().getVersion();
                if (isNewerVersion(latestTag,currentVersion)){
                    ClientDetectorExtra.getServerImplementation().global().run(()->{
                        ClientDetectorExtra.getInstance().getLogger().warning("=================================================");
                        ClientDetectorExtra.getInstance().getLogger().warning(" Une nouvelle version de ClientDetectorExtra\n est disponible !");
                        ClientDetectorExtra.getInstance().getLogger().warning(" Version installée: v"+currentVersion);
                        ClientDetectorExtra.getInstance().getLogger().warning(" Nouvelle version: "+latestTag);
                        ClientDetectorExtra.getInstance().getLogger().warning(" Téléchargement: https://github.com/"+USER+"/"+REPO+"/releases/latest");
                        ClientDetectorExtra.getInstance().getLogger().warning("=================================================");
                    });
                }
            }catch(Exception e){
                ClientDetectorExtra.getInstance().getLogger().warning("Impossible de vérifier les mises à jour: "+e.getMessage());
            }
        });
    }

    public static@NotNull String getLatestTag()throws Exception{
        final URL url=java.net.URI.create("https://api.github.com/repos/"+USER+"/"+REPO+"/releases/latest").toURL();
        final HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Accept","application/vnd.github+json");
        connection.setRequestProperty("User-Agent",REPO+"-UpdateChecker");

        final BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final StringBuilder json=new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
            json.append(line);
        }reader.close();

        return json.toString().split("\"tag_name\":\"")[1].split("\"")[0];
    }
    public static boolean isNewerVersion(@NotNull String latest, @NotNull String current) {
        try {
            String[] latestParts = latest.replace("v", "").split("\\.");
            String[] currentParts = current.replace("v", "").split("\\.");

            int length = Math.max(latestParts.length, currentParts.length);
            for (int i = 0; i < length; i++) {
                int vLatest = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                int vCurrent = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;

                if (vLatest > vCurrent) return true;
                if (vLatest < vCurrent) return false;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}