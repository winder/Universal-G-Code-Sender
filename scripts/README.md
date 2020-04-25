### Setup

- `pip install -r requirements.txt`

### Scripts

# update_languages.py

This script utilizes the POEditor REST API to update all `MessagesBundle_*.properties` files with the latest localizations.

Configuration:
* Environment variable **POEDITOR_API_KEY** - POEditorAPI key, UGS is a public project so any API key should do.
* **LANGUAGES** dict - inside the script is a dictionary to map the POEditor language code to the UGS MessagesBundle file.

Running:
Ensure you're **POEDITOR_API_KEY** environment variable is set then run `./update_languages.py --upload --download` from the scripts directory.

Adding a new language:
Update the **LANGUAGES** dictionary inside the script.

# svg_to_icons.sh

This converts an svg to multiple png files with a special naming convention so that they can be used at different resolutions and different themes.

This script can be a little picky, sometimes you need to open the SVG with inkscape and save it as a plain SVG, or inkscape SVG, or set the fill color to solid black.

Most icons are stored in `ugs-platform-ugscore`, if you add an icon elsewhere just update the path:
```
./scripts/svg_to_icons.sh ./ugs-platform/ugs-platform-ugscore/src/main/resources/resources/icons
```
