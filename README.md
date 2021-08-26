# SteamAutoDelete

Deletes all your installed steam games you haven't played for more than X days.

## Usage
`steam_auto_delete.py [--all] [--delete]`
<dl>
<dt>--all</dt><dd>show all games</dd>
<dt>--delete</dt><dd>delete games</dd>
</dl>

## Configure
Edit `steam_auto_delete_conf.py` to configure your steam library, whitelist and so on:

```
# your steam libraries
steam_libraries = ['C:\\Program Files (x86)\\Steam', 'D:\steam']

# delete a game if you haven't played it for this number of days
delete_days = 365

# never delete this games (IDs)
whitelist = [
    228980, # Steamworks Common Redistributables
    250820, # SteamVR
    719950, # Windows Mixed Reality for SteamVR
    ]
```
