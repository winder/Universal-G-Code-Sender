### Setup

- `pip install -r requirements.txt`

### Scripts

# update_languages.py

This script utilizes the POEditor REST API to update all `MessagesBundle_*.properties` files with the latest localizations.

Configuration:
* Environment variable **POEDITOR_API_KEY** - POEditorAPI key, UGS is a public project so any API key should do.
* **LANGUAGES** dict - inside the script is a dictionary to map the POEditor language code to the UGS MessagesBundle file.

Running:
Ensure you're **POEDITOR_API_KEY** environment variable is set then run `./update_languages.py` from the scripts directory.

Adding a new language:
Update the **LANGUAGES** dictionary inside the script.
