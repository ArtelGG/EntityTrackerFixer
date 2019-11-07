## EntityTrackerFixer

### Description:
The goal of this plugin is to try and resolve the issues with the internal server's entity tracker.
Further optimizing your server to ensure it's running as smoothly as possible, given the circumstances with 1.14+.

### Commands & Permissions:
* `/entitytrackerfixer`
  * **Aliases:** `/etf`
  * **Permission:** `entitytrackerfixer.admin`
  * **Arguments:**
    * `reload` - Reloads the configuration file and restarts all tasks with the updated settings.
    * `debug` - Displays the server's current TPS and information regarding the configuration file.

### How to Contribute:
Unfortunately, due to the code we use, there are no Maven repositories available for us to make this project easily accessible.

Contributing will require you to obtain server software and add it as a library to your IDE of choice, to obtain packages such as `net.minecraft.server` and so fourth.
[This](https://yivesmirror.com/) should get you started.

This is necessary due to how Mojang handles code (re)distribution.

### Resources:
* [Server Optimization Guide](https://www.spigotmc.org/threads/283181/)