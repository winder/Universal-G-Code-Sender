#!/bin/bash

# ./scripts/svg_to_icons.sh ./ugs-platform/ugs-platform-ugscore/src/main/resources/resources/icons

if [ "$#" -ne 1 ] || ! [ -d "$1" ]; then
  echo "Usage: $0 DIRECTORY" >&2
  exit 1
fi

cd "$( realpath "$1" )"

if [ -f /Applications/Inkscape.app/Contents/Resources/bin/inkscape ]; then
	alias inkscape="/Applications/Inkscape.app/Contents/Resources/bin/inkscape"
else
    echo "Inkscape not found"
    exit 1
fi

TRUE_BLACK="#000000"
BLACKISH="#353a40"
ALWAYS_BLACK="#010101"
LIGHT_BLUE="#0ac9f1"
DARK_BLUE="#41818f"

# Make sure nobody is using black instead of blackish
for INPUT in *.svg ; do 
  sed -e "s/$TRUE_BLACK/$BLACKISH/g" "${PWD}/${INPUT}" > "${PWD}/temp"
  mv "${PWD}/temp" "${PWD}/${INPUT}"
done

# Generate _dark and _disabled_dark vectors
for INPUT in *.svg ; do
  FILENAME=${INPUT%.*}
  sed -e "s/$BLACKISH/$LIGHT_BLUE/g" -e "s/$ALWAYS_BLACK/$BLACKISH/g" "${PWD}/${INPUT}" > "${PWD}/${FILENAME}_dark.svg"
  sed -e "s/$BLACKISH/$DARK_BLUE/g" -e "s/$ALWAYS_BLACK/$BLACKISH/g" "${PWD}/${INPUT}" > "${PWD}/${FILENAME}_disabled_dark.svg"
done

# Generate png files in various sizes
for INPUT in *.svg ; do
	FILENAME=${INPUT%.*}
	SUFFIX=".png"
	if [[ ${FILENAME} = *"disabled_dark" ]]; then
		FILENAME="${FILENAME/_disabled_dark/}"
		SUFFIX="_disabled_dark.png"
	elif [[ ${FILENAME} = *"_dark" ]]; then
		FILENAME="${FILENAME/_dark/}"
		SUFFIX="_dark.png"
	fi

	inkscape --export-area-page --file "${PWD}/${INPUT}" -w 16 -h 16 --export-png "${PWD}/${FILENAME}${SUFFIX}"
	inkscape --export-area-page --file "${PWD}/${INPUT}" -w 24 -h 24 --export-png "${PWD}/${FILENAME}24${SUFFIX}"
	inkscape --export-area-page --file "${PWD}/${INPUT}" -w 32 -h 32 --export-png "${PWD}/${FILENAME}32${SUFFIX}"
done
