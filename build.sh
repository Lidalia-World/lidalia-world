#!/usr/bin/env sh

set -eu

main() {

  rm -rf build

  if ! dockerBuild "$@"; then
    dockerBuild \
      --target build-output \
      --build-arg "CACHE_BUSTER=$(date +%s)" \
      "$@"
    echo "Output can be found in build"
    if [ -e build/failed ]; then
      exit "$(cat build/failed)";
    else
      echo "Docker build passed the second time! Very odd... Must be something flaky."
      exit 1
    fi
  fi
}

dockerBuild() {
  if git diff HEAD --quiet; then
    hash=$(git rev-parse HEAD)
  else
    hash=''
  fi

  docker build . \
    --build-arg "CI=${CI:-}" \
    --build-arg "GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)" \
    --build-arg "GIT_HASH=$hash" \
    --build-arg "VERSION=$(git tag --points-at HEAD | head -n1)" \
    --build-arg "BUILD_NUMBER=${BUILD_NUMBER:-}" \
    --output build \
    "$@"
}

main "$@"
