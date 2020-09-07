#!/bin/bash
#
# Usage: ./scripts/svg_to_icons.sh ./ugs-platform/ugs-platform-ugscore/src/main/resources/resources/icons

if [ "$#" -ne 1 ] || ! [ -d "$1" ]; then
  echo "Usage: $0 DIRECTORY" >&2
  exit 1
fi

cd "$( realpath "$1" )"

TRUE_BLACK="#000000"
BLACKISH="#353a40"
ALWAYS_BLACK="#010101"
LIGHT_BLUE="#0ac9f1"
DARK_BLUE="#41818f"


# Make sure nobody is using black instead of blackish
shopt -s extglob
for INPUT in !(*_dark).svg ; do
  echo "Processing: ${INPUT}"

  # Fix black to blackish color
  sed -e "s/$TRUE_BLACK/$BLACKISH/g" "${PWD}/${INPUT}" > "${PWD}/temp"
  mv "${PWD}/temp" "${PWD}/${INPUT}"

  # Generate dark themed files
  FILENAME=${INPUT%.*}
  sed -e "s/$BLACKISH/$LIGHT_BLUE/g" -e "s/$ALWAYS_BLACK/$BLACKISH/g" "${PWD}/${INPUT}" > "${PWD}/${FILENAME}_dark.svg"
  sed -e "s/$BLACKISH/$DARK_BLUE/g" -e "s/$ALWAYS_BLACK/$BLACKISH/g" "${PWD}/${INPUT}" > "${PWD}/${FILENAME}_disabled_dark.svg"
done