# 🛡️ ClientDetectorExtra

**ClientDetectorExtra** est un plugin de détection de clients (brands) haute performance, conçu pour les serveurs Minecraft modernes. Il permet d'identifier précisément quel client (Lunar, Forge, Fabric, etc.) tes joueurs utilisent, tout en supportant les joueurs Bedrock via Geyser.

---

## 🚀 Caractéristiques

* **⚡ Performance Extrême :** Système de mise en cache (`msgCache`) pour éviter les lectures de fichiers répétitives.
* **🌌 Compatibilité Folia :** Utilise `foliascheduler` pour une exécution thread-safe sur les serveurs multithreadés.
* **🎨 Full HEX & MiniMessage :** Support complet des couleurs Hexadécimales, des dégradés (gradients) et des styles modernes.
* **📱 Support Geyser/Bedrock :** Détecte automatiquement si un joueur provient de l'édition Bedrock.
* **📢 Notifications Flexibles :** Alertes configurables pour la console et les administrateurs en ligne.

---

## 🛠️ Spécifications Techniques

| Caractéristique | Détails |
| :--- | :--- |
| **Version Java** | Java 21 |
| **API Minecraft** | Paper / Folia 1.17+ |
| **Forks Supportés** | Paper, Folia, Canvas, Purpur, Pufferfish |
| **Dépendances Shaded** | `foliascheduler`, `fastboard`, `adventure-minimessage` |

---

## 📂 Configuration (`config.yml`)

Le plugin utilise le format **MiniMessage**. Vous pouvez utiliser des balises comme `<gradient:#55ffff:#5555ff>` ou des codes HEX `<#FFFFFF>`.

```yaml
notify-console: true
notify-admin: true
permission: "clientdetector.admin"

messages:
  prefix: "<#888888>[<gradient:#55ffff:#5555ff>ClientDetector</gradient><#888888>] <#ffffff>"
  reload: "<#55ff55>La configuration a été rechargée !"
  no-permission: "<#ff5555>Tu n'as pas la permission d'utiliser cela."
  player-info: "<#aaaaaa>Joueur: <#55ffff>%player% <#888888>| <#aaaaaa>Client: <#55ffff>%brand% <#888888>| <#aaaaaa>Bedrock: <#55ffff>%is_bedrock%"
  player-check: "<#aaaaaa>Infos sur <#55ffff>%target% <#888888>: <#ffffff>Client: <#55ffff>%brand% <#888888>| <#aaaaaa>Bedrock: <#55ffff>%is_bedrock%"
  player-not-found: "<#ff5555>Ce joueur n'est pas en ligne."
```
---

## 💻 Utilisation de l'API

Si vous êtes un développeur, vous pouvez intégrer **ClientDetectorExtra** à votre propre plugin.

### 1. Accéder à l'API
```java
import santexfr.api.ClientDetectorAPI;
import santexfr.ClientDetectorExtra;

// Récupérer l'instance de l'API
ClientDetectorAPI api = ClientDetectorExtra.getApi();

// Vérifier si un joueur est sur Bedrock
boolean isBedrock = api.isBedrock(player);

// Récupérer le nom du client (Brand)
String brand = api.getClientBrand(player);
```

## 📜 Commandes & Permissions

* `/clientdetector reload` : Recharge la configuration.
* `/clientdetector info <joueur>` : Affiche les détails d'un joueur spécifique.
* **Alias :** `/cdetect`

---

## 🛠️ Compilation

Pour compiler le projet vous-même :
1. Clonez le dépôt.
2. Assurez-vous d'avoir **Maven** et **Java 21** installés.
3. Exécutez la commande suivante :
```bash
mvn clean package
```
