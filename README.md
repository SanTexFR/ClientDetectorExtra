# 🛡️ ClientDetectorExtra

**ClientDetectorExtra** est un plugin de détection de clients (brands) haute performance, conçu pour les serveurs Minecraft modernes. Il permet d'identifier précisément quel client (Lunar, Forge, Fabric, etc.) tes joueurs utilisent, tout en supportant les joueurs Bedrock via Geyser.

---

## 🚀 Caractéristiques

- **⚡ Performance Extrême :** Système de mise en cache (`msgCache`) pour éviter les lectures de fichiers répétitives.
- **🌌 Compatibilité Folia :** Utilise `foliascheduler` pour une exécution thread-safe sur les serveurs multithreadés.
- **🎨 Full HEX & MiniMessage :** Support complet des couleurs Hexadécimales, des dégradés (gradients) et des styles modernes.
- **📱 Support Geyser/Bedrock :** Détecte automatiquement si un joueur provient de l'édition Bedrock.
- **📢 Notifications & Webhooks :** Alertes configurables en jeu, sur la console et directement dans un salon Discord via Webhook.
- **⛔ Blacklist & Actions Automatiques :** Restriction de clients/mods non autorisés avec sanctions personnalisées (`KICK`, `BAN`, `COMMAND`, `NOTIFY`).
- **📋 Export de Rapport (Dump) :** Génération rapide de rapports de données en ligne via l'API `pastes.dev`.

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
check-for-update: true
notify-console: true
notify-admin: true
permission: "clientdetector.admin"

messages:
  #GUI
  gui-main-title: "<#888888>▶ Clients (Page %page%)"
  gui-sub-title: "<#888888>▶ %brand% (Page %page%)"
  gui-next-page: "<#ffa500>« Page Précédente"
  gui-previous-page: "<#ffa500>Page Suivante »"
  gui-back-button: "<#ff5555>Retour au Menu Principal"
  gui-action-title: "<#888888>Actions: <#55ffff>%player%"
  gui-action-teleport: "<#55ff55>➤ Se téléporter"
  gui-client-lore:
    - "<#aaaaaa>Cliquez pour voir les joueurs."
    - ""
    - "<#ffffff>Joueurs : <#55ffff>%count%"
  gui-player-lore:
    - "<#aaaaaa>UUID: <#ffffff>%uuid%"
    - "<#aaaaaa>Bedrock: <#55ffff>%is_bedrock%"

  prefix: "<#888888>[<gradient:#55ffff:#5555ff>ClientDetector</gradient><#888888>] <#ffffff>"
  reload: "<#55ff55>La configuration a été rechargée !"
  no-permission: "<#ff5555>Tu n'as pas la permission d'utiliser cela."
  player-info: "<#aaaaaa>Joueur: <#55ffff>%player% <#888888>| <#aaaaaa>Client: <#55ffff>%brand% <#888888>| <#aaaaaa>Bedrock: <#55ffff>%is_bedrock%"
  player-check: "<#aaaaaa>Infos sur <#55ffff>%target% <#888888>: <#aaaaaa>Client: <#55ffff>%brand% <#888888>| <#aaaaaa>Bedrock: <#55ffff>%is_bedrock%"
  player-not-found: "<#ff5555>Ce joueur n'est pas en ligne."

webhooks:
  enabled: true
  url: "[https://discord.com/api/webhooks/](https://discord.com/api/webhooks/)..."
  all-clients: false # Si true, ignore la liste et alerte pour tout le monde
  alert-bedrock: true
  alert-brands:
    - "forge"
    - "fabric"
  message: "🔔 **%player%** a rejoint avec **%brand%** (Bedrock: %is_bedrock%)"

blacklist:
  enabled: true
  restricted-brands:
    - "meteor"
    - "wurst"
    - "cheat"
  actions:
    - "NOTIFY"
    - "KICK:<red>Les clients modifiés ne sont pas autorisés sur ce serveur !"