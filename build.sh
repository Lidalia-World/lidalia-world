#!/usr/bin/env bash

set -eu

main() {

  rm -rf build

  dockerBuild "$@"

  if [ -d build ]; then
    echo "Output can be found in ./build"
  fi
  if [ -e build/failed ]; then
    exit "$(cat build/failed)";
  fi
}

dockerBuild() {

  if git diff HEAD --quiet; then
    hash=$(git rev-parse HEAD)
  else
    hash=''
  fi

  version_tag=$(git tag --points-at HEAD | head -n1)
  git_branch=$(git rev-parse --abbrev-ref HEAD)

  build_args=(
    --build-arg "CI=${CI:-}"
    --build-arg "GIT_BRANCH=$git_branch"
    --build-arg "GIT_HASH=$hash"
    --build-arg "VERSION=${version_tag#v}"
    --build-arg "BUILD_NUMBER=${BUILD_NUMBER:-}"
    --build-arg "CACHE_BUSTER=$(date +%s)"
  )

  if [[ ! " $* " =~ --output([ =]|$) ]]; then
    build_args+=(--output build)
  fi

  # Append user-provided args
  build_args+=("$@")

  set +e
  set -x

  docker build . "${build_args[@]}"

  status=$?

  set +x
  set -e

  return "$status"
}

main "$@"
