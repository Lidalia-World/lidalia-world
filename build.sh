#!/usr/bin/env bash

set -euo pipefail

main() {
  export BUILDKIT_PROGRESS=plain
  export PROGRESS_NO_TRUNC=1

  rm -rf build

  if docker build . --output build; then
    echo "Output can be found in build/artifacts"
  else
    docker build . \
      --target build-output \
      --output build
    exit "$(cat build/failed)";
  fi
}

main "$@"
