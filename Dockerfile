# syntax=docker/dockerfile:1.13.0
ARG username=worker
ARG work_dir=/home/$username/work
ARG GRADLE_TASK=build

# Copy across all the build definition files in a separate stage
# This will not get any layer caching if anything in the context has changed, but when we
# subsequently copy them into a different stage that stage *will* get layer caching. So if none of
# the build definition files have changed, a subsequent command will also get layer caching.
FROM --platform=$BUILDPLATFORM alpine AS gradle-files
RUN --mount=type=bind,target=/docker-context \
    mkdir -p /gradle-files/gradle && \
    cd /docker-context/ && \
    find . -name "*.gradle" -exec cp --parents "{}" /gradle-files/ \; && \
    find . -name "*.gradle.kts" -exec cp --parents "{}" /gradle-files/ \; && \
    find . -name "libs.versions.toml" -exec cp --parents "{}" /gradle-files/ \; && \
    find . -name ".editorconfig" -exec cp --parents "{}" /gradle-files/ \; && \
    find . -name "gradle.properties" -exec cp --parents "{}" /gradle-files/ \; && \
    find . -name "*module-info.java" -exec cp --parents "{}" /gradle-files/ \;


FROM --platform=$BUILDPLATFORM eclipse-temurin:24-jdk-alpine-3.22 AS base_builder

ARG username
ARG gid=1000
ARG uid=1001

RUN addgroup --system $username --gid $gid && \
    adduser --system $username --ingroup $username --uid $uid

USER $username

ARG work_dir
RUN mkdir -p $work_dir
WORKDIR $work_dir

ARG tmp_user_home=/tmp/gradle/home
ARG tmp_gradle_user_home=$tmp_user_home/.gradle
ARG tmp_gradle_user_home_cache_dir=$tmp_gradle_user_home/caches

RUN mkdir -p $tmp_gradle_user_home_cache_dir

# Download gradle in a separate step to benefit from layer caching
COPY --link --chown=$uid gradle/wrapper gradle/wrapper
COPY --link --chown=$uid gradlew gradlew
RUN  ./gradlew --version && \
     cp -R ~/.gradle $tmp_user_home

ARG build_cache_dir=/home/$username/.gradle/caches/build-cache-1

RUN mkdir -p $build_cache_dir
RUN mkdir -p $work_dir/.gradle

ENV GRADLE_OPTS="\
-Dorg.gradle.daemon=false \
-Dorg.gradle.vfs.watch=false \
-Dorg.gradle.console=plain \
"

# Build the configuration cache & download all deps in a single layer
COPY --link --chown=$uid --from=gradle-files /gradle-files ./
COPY --link --chown=$uid gradle gradle

ARG GRADLE_TASK
ARG GRADLE_ARGS

RUN --mount=type=cache,gid=$gid,uid=$uid,target=$work_dir/.gradle \
    --mount=type=cache,gid=$gid,uid=$uid,target=$tmp_gradle_user_home_cache_dir \
    GRADLE_USER_HOME=$tmp_gradle_user_home \
    ./gradlew $GRADLE_ARGS \
      --dry-run \
      build && \
    cp -R $tmp_gradle_user_home_cache_dir ~/.gradle

RUN rm -rf $tmp_user_home

COPY --link --chown=$uid . .


FROM --platform=$BUILDPLATFORM base_builder AS unfailing-build
ARG GRADLE_ARGS
ARG CACHE_BUSTER
ARG GRADLE_TASK

RUN echo "$CACHE_BUSTER" > /dev/null

RUN --mount=type=cache,gid=$gid,uid=$uid,target=$work_dir/.gradle \
    --mount=type=cache,gid=$gid,uid=$uid,target=$build_cache_dir \
    --network=none \
    ./gradlew $GRADLE_ARGS --offline $GRADLE_TASK || (status=$?; mkdir -p build && echo $status > build/failed)


FROM --platform=$BUILDPLATFORM scratch AS build-output
ARG work_dir

COPY --link --from=unfailing-build $work_dir/build .


FROM --platform=$BUILDPLATFORM base_builder AS builder
ARG GRADLE_ARGS
ARG GRADLE_TASK

RUN --mount=type=cache,gid=$gid,uid=$uid,target=$work_dir/.gradle \
    --mount=type=cache,gid=$gid,uid=$uid,target=$build_cache_dir \
    --network=none \
    ./gradlew $GRADLE_ARGS --offline $GRADLE_TASK


FROM --platform=$BUILDPLATFORM scratch
ARG work_dir

COPY --link --from=builder $work_dir/build .
