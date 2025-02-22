#!/usr/bin/env bash

set -euo pipefail

main() {
  export BUILDKIT_PROGRESS=plain
  export PROGRESS_NO_TRUNC=1

  rm -rf build

  if docker build . \
      --build-arg "GRADLE_ARGS=$*" \
      --output build; then
    echo "Output can be found in build/artifacts"
  else
    docker build . \
        --build-arg "GRADLE_ARGS=$*" \
        --target build-output \
        --output build
    exit "$(cat build/failed)";
  fi
}

main "$@"
