#!/bin/bash

# Getting AOC session token:
# https://github.com/wimglenn/advent-of-code-wim/issues/1#issue-193321235


usage(){
    cat <<EOF
Usage: $0 [-h] [-c] [-l] [-n] <day>

Download AOC puzzle input to dedicated directory.
If no DAY provided, download current day's input.

Options:
  -h    print this message and exit
  -c    cat file to stdout
  -l    print file to less
  -n    don't download file
EOF
}

CURL=true
CMD=

while getopts "clhn" opt; do
    case ${opt} in
        h) usage; exit ;;
        c) CMD=cat   ;;
        l) CMD=less  ;;
        n) CURL=false  ;;
    esac
done

shift $(( OPTIND - 1 ))

#* set SESSION COOKIE
. aoc_token

#* setup url & path
DAY_URL=${1:-$(date +%-d)}
[[ -n $1 ]] && DAY_FILE=$(printf "%02d" "$1") || DAY_FILE=$(date "+%d")
PUZZLE_FILE="inputs/day$DAY_FILE"
PUZZLE_URL="https://adventofcode.com/2018/day/${DAY_URL}/input"

[[ -d "inputs" ]] || mkdir "inputs"

#* exit if day is in future
if (( $DAY_URL > $(date +%-d ))); then
    echo "Don't be hasty!"
    exit 1
fi

#* download file
if [[ $CURL = true ]]; then
    curl "${PUZZLE_URL}" -H "cookie: session=${AOC_SESSION_COOKIE}" -o "${PUZZLE_FILE}"
fi

#* run optional command (cat, less)
[[ -n $CMD ]] && $CMD $PUZZLE_FILE

