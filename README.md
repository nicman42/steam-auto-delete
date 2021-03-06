# SteamAutoDelete

Deletes all your installed steam games you haven't played for more than X days.

## Usage
`steam_auto_delete.py [--all] [--delete]`
<dl>
<dt>--all</dt><dd>List all games. Without this argument, only games for deletion are displayed.</dd>
<dt>--delete</dt><dd>Delete games. Without this argument nothing is deleted.</dd>
</dl>

## Configure
Edit `steam_auto_delete_conf.py` to configure your steam library, whitelist and so on:

```
# your steam libraries
steam_libraries = ['C:\\Program Files (x86)\\Steam', 'D:\\steam']

# delete a game if you haven't played it for this number of days
delete_days = 365

# never delete this games (IDs)
exclude = [
    228980, # Steamworks Common Redistributables
    250820, # SteamVR
    719950, # Windows Mixed Reality for SteamVR
    ]
```
