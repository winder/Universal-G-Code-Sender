#!/bin/bash

if [ -f /Applications/Inkscape.app/Contents/Resources/bin/inkscape ]; then
	alias inkscape="/Applications/Inkscape.app/Contents/Resources/bin/inkscape"
fi

# Make sure nobody is using black instead of blackish
for INPUT in *.svg ; do 
  sed -e "s/#000000/#353a40/" "${PWD}/${INPUT}" > "${PWD}/temp"
  mv  "${PWD}/temp" "${PWD}/${INPUT}"
done

# Generate _dark and _disabled_dark vectors
for INPUT in *.svg ; do 
	FILENAME=${INPUT%.*}
  sed -e "s/#353a40/#0ac9f1/" "${PWD}/${INPUT}" > "${PWD}/${FILENAME}_dark.svg"
  sed -e "s/#353a40/#41818f/" "${PWD}/${INPUT}" > "${PWD}/${FILENAME}_disabled_dark.svg"
done

# Generage png files in various sizes
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
