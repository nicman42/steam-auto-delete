#!/usr/bin/env python3

import sys
import os
import re
import datetime
import shutil
try:
    import steam_auto_delete_conf as conf
    steam_libraries = conf.steam_libraries
    delete_days = conf.delete_days
    exclude = [str(id) for id in conf.exclude]
except ImportError:
    steam_libraries = ['C:\\Program Files (x86)\\Steam']
    delete_days = 365
    exclude = []

delete_date = datetime.datetime.now() - datetime.timedelta(days=delete_days)

do_delete = '--delete' in sys.argv[1:]
show_all = '--all' in sys.argv[1:]


LOG_FILE = os.path.join(os.path.dirname(sys.argv[0]), 'steam_auto_delete.log')

print('----------------------------------------')
print(f'delete games not played after {delete_date}')
print('----------------------------------------')

# get last played times
last_played_times = {}
for steam_library in steam_libraries:
    user_data = os.path.join(steam_library, 'userdata')
    if not os.path.exists(user_data):
        continue
    for user_data_directory in [os.path.join(user_data, d) for d in os.listdir(user_data)]:
        vdf_path = os.path.join(user_data_directory, 'config', 'localconfig.vdf')    
        if not os.path.exists(vdf_path):
            continue
        with open(vdf_path, encoding='utf-8') as fp:
            for line in fp.readlines():
                m = re.match(r'\s*"(.*?)"\s*("(.*?)")?', line)
                if not m:
                    continue
                key = m.group(1)
                value = m.group(3)
                if not value:
                    id = key
                    continue

                if key == 'LastPlayed':
                    last_played_times[id] = max(int(value), last_played_times.get(id) or 0)

last_played_times = {key:datetime.datetime.fromtimestamp(value) for (key,value) in last_played_times.items()}

# get installed games
games = []
for steam_library in steam_libraries:
    steamapps = os.path.join(steam_library, 'steamapps')
    if not os.path.exists(steamapps):
        continue
    for acf in [f for f in os.listdir(steamapps) if f.endswith('.acf')]:
        acf_path = os.path.join(steamapps, acf)
        id = re.match(r'.*_(.*).acf', acf).group(1)
        game = {
            'id': id,
            'acf': acf_path,
            'last_played_time': last_played_times.get(id),
        }
        with open(acf_path) as fp:
            for line in fp.readlines():
                m = re.match(r'\s*"(.*)"\s*"(.*)"', line)
                if not m:
                    continue
                key = m.group(1)
                value = m.group(2)
                
                if key == 'name':
                    game['name'] = value
                elif key == 'installdir':
                    game['installdir'] = os.path.join(os.path.dirname(acf_path), 'common', value)
                    # fallback for name
                    if game['name'].startswith("appid_"):
                        game['name'] = value
        games.append(game)

games.sort(key = lambda g: g['last_played_time'] or datetime.datetime(datetime.MINYEAR, 1, 1))

# list/delete games
for game in games:
    delete_game = not game['id'] in exclude and game['last_played_time'] and game['last_played_time']<delete_date

    if delete_game or show_all:
        print(game['name'])
        print(f"\tid: {game['id']}")
        print(f"\tlast played: {game['last_played_time']}")

    if game['id'] in exclude:
        if show_all:
            print('\tgame is excluded from deletion')
    elif delete_game:
        if do_delete:
            with open(LOG_FILE, "a") as fp:
                fp.write(f"{datetime.datetime.now()}: delete '{game['name']}' ({game['id']})\n")
            print(f"\tdelete '{game['acf']}'")
            os.remove(game['acf'])

            print(f"\tdelete '{game['installdir']}'")
            shutil.rmtree(game['installdir'])
        else:
            print(f"\tacf: '{game['acf']}'")
            print(f"\tinstalldir: '{game['installdir']}'")
            print("\tcall with '--delete' to delete this game")

print('----------------------------------------')
print(f'usage: {os.path.basename(sys.argv[0])} [--all] [--delete]')
print('--all\tshow all games')
print('--delete\tdelete games')
print('----------------------------------------')
#input('press ENTER to quit')
