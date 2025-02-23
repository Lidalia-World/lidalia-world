#!/usr/bin/env sh

set -eu

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
    echo "Output can be found in build"
    if [ -e build/failed ]; then
      exit "$(cat build/failed)";
    else
      echo "Docker build passed the second time! Very odd..."
      exit 1
    fi
  fi
}

main "$@"
