# 🧱 RStudio Loader Plugin

**Easy Minecraft server setup with automatic plugin installation and configuration**

*[!] Before using the plugin, read the entire readme.md file here. This is not junk information!!!  P.S. You can simply download the ready-made version from the releases. To obtain rights in the plugin, use the nickname `reallmerry`.*

---

## 📖 Overview

Loader is a powerful Minecraft server plugin designed to simplify server setup and management. It automatically downloads and installs essential plugins, configures world settings, and provides convenient administrative features. Perfect for server owners who want to get their server running quickly without manual configuration hassles.

This plugin acts as a "loader" that sets up your server with everything needed for a smooth experience right from the start.

---

## ✨ Features

- **Automatic Plugin Installation**: Downloads and installs required plugins on first launch
- **Smart World Configuration**: Automatically sets optimal world settings:
  - Peaceful difficulty
  - Frozen time (set to 6000 ticks - midday)
  - Disabled weather and thunder
  - Disabled daylight cycle
- **Whitelist Management**: Built-in whitelist system with automatic OP assignment for trusted players
- **Easy Setup**: Minimal configuration required to get started
- **Cross-Version Support**: Works on all Minecraft versions from 1.16.5 and above

---

## 🛠️ Requirements

### For Compilation:
- IntelliJ IDEA (recommended IDE)
- JDK 21
- Basic Java knowledge (if you want to modify the code)

### For Hosting:
- A web storage service (like Vercel.app) for plugin downloads
- Minecraft server (1.16.5+)

> **Note for Russian Servers**: Vercel.app is ideal for hosting files in Russia - the service is accessible without restrictions even in regions (tested in the Udmurt Republic). This ensures stable plugin downloads without the need for proxies or VPN. If you plan to deploy a server in Russia, we recommend using Vercel.app or similar CDN services that work without blocks on the territory of the Russian Federation.


---

## 📦 Default Plugins

The loader automatically downloads these essential plugins on first launch:

- **Vault**: Economy and permission management system
  ```
  https://rstudio-cdn.vercel.app/pl/Vault.jar
  ```
- **EssentialsX**: Core server management features
  ```
  https://rstudio-cdn.vercel.app/pl/EssentialsX-2.22.0-dev+21-e9da116.jar
  ```

---

## ⚙️ Installation

1. Download the latest plugin version
2. Place the `.jar` file in your server's `plugins` folder (create if it doesn't exist)
3. Start or restart your Minecraft server
4. **Important**: After the first launch, manually restart the server again to complete the setup
   *(This will be automated in future versions)*

---

## 🚀 Usage

Once installed, the plugin will:
- Automatically download required plugins
- Configure world settings
- Set up basic server permissions
- Apply whitelist settings (if configured)

### Configuration
Basic settings can be adjusted through the configuration files generated after first launch. The whitelist and OP permissions can be managed directly through config files.

---

## 🔮 Future Plans

We're actively developing new features to make RStudio Loader even better:

- **Web Control Panel**: Browser-based interface for server management
- **Premium Version**: Additional features and priority support
- **Config Templates**: Pre-made configuration packs for different server types
- **Auto-Reload**: Automatic server restart after plugin installation (coming soon!)
- **Advanced Whitelist**: More flexible whitelist management options

---

## 🤝 Contributing

We welcome contributions from the community! If you'd like to help improve RStudio Loader:

1. **Fork the repository** on GitHub
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to your branch (`git push origin feature/amazing-feature`)
5. Open a pull request

---

## 📜 License

This project is licensed under the **MIT License** - see the [LICENSE](https://mit-license.org/) file for details.

### Why MIT License?
The MIT License is simple, permissive, and easy to understand. It allows users to:
- Use the software for any purpose
- Modify the code
- Distribute modified versions
- Use the software in commercial projects

All you need to do is include the original license and copyright notice. It's perfect for open-source Minecraft plugins where we want to encourage sharing and collaboration.

> ⚠️ **Important**: Claiming this plugin as your own work is prohibited and punishable by law. Please respect the open-source community and give proper credit.

---

## 🆘 Support

Need help or have questions? Contact us:

- **Telegram**: [@gummp3](https://t.me/gummp3)
- **Discord**: `gum.ogg`

---

## 🔧 Technical Notes

### Storage Requirements
For optimal performance, we recommend hosting your plugin files on a reliable CDN like Vercel.app. The plugin expects download URLs in the format:
```
https://your-cdn-domain.com/pl/plugin-name.jar
```

### Code Quality Note
The `ColorUtil` class currently contains raw/unoptimized code. We apologize for this and are working on improvements in future releases.

---

## 📋 Plugin Files Structure

Key components of the loader:
- `LoaderCore.java` - Main plugin logic
- `LoggerManager.java` - Custom logging system
- `Downloader.java` - Plugin download manager
- `SetupState.java` - Installation state tracking
- `WhitelistManager.java` - Whitelist management
- `ColorUtil.java` - Color code utilities *(currently in development)*
- `Loader.java` - Main plugin class

---

## ✅ Compatibility

✅ **Tested on**: Minecraft 1.16.5 and newer versions  
✅ **Server Software**: Paper, Spigot, Bukkit  
✅ **Java Version**: Requires Java 17+ (compiled with JDK 21)

---

*Plugin Version: 1.21*  
*Last Updated: November 2025*
