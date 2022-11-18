# Dreamvisitor
A plugin created by Bog for WoF:TNW to add various features.

## Features
- Discord chat to Minecraft chat & vise versa
- Automatic whitelist from Discord
- Tempban from Discord
- Log server start/stop times

## Using Dreamvisitor
###  Discord Commands
#### Admin Commands
- `/setwhitelistchannel <channel>` - sets the channel to listen for whitelist requests.
- `/setgamechatchannel <channel>` - sets the channel to allow Discord/Minecraft chat.
- `/setmemberrole <role>` - sets the role to give users upon whitelist.
- `/setstep3role <role>` - sets the role to remove from users upon whitelist.
- `/tempban <username> <hours> <reason>` - bans a Minecraft user for a specified duration.
#### User Commands
- `/list` - List the online players. Only works in the assigned game chat channel.
- `/msg <user> <message>` - Messages a user on the Minecraft server. Only works in the assigned game chat channel.
### Minecraft Commands
#### Admin Commands
- `/pausechat` - Pauses chat for non-OPs
- `/radio` - Send a message to all OPs.
#### User Commands
- `/discord` - Toggle Discord message visibility.
##
This plugin is developed specifically for WoF:TNW. No builds will be provided as releases.
