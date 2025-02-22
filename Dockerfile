# syntax=docker/dockerfile:1.13.0
ARG username=worker
ARG work_dir=/home/$username/work

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


FROM --platform=$BUILDPLATFORM eclipse-temurin:21.0.6_7-jdk-alpine-3.21 AS base_builder

ARG username
ARG gid=1000
ARG uid=1001

RUN addgroup --system $username --gid $gid && \
    adduser --system $username --ingroup $username --uid $uid

USER $username

ARG work_dir
RUN mkdir -p $work_dir
WORKDIR $work_dir

# Download gradle in a separate step to benefit from layer caching
COPY --link --chown=$uid gradle/wrapper gradle/wrapper
COPY --link --chown=$uid gradlew gradlew
RUN  ./gradlew --version

ARG gradle_cache_dir=/home/$username/.gradle/caches

RUN mkdir -p $gradle_cache_dir

ENV GRADLE_OPTS="\
-Dorg.gradle.daemon=false \
-Dorg.gradle.logging.stacktrace=all \
-Dorg.gradle.vfs.watch=false \
-Dorg.gradle.console=plain \
"

# Build the configuration cache & download all deps in a single layer
COPY --link --chown=$uid --from=gradle-files /gradle-files ./
COPY --link --chown=$uid gradle gradle
RUN --mount=type=cache,gid=$gid,uid=$uid,target=$work_dir/.gradle \
    --mount=type=cache,gid=$gid,uid=$uid,target=$gradle_cache_dir \
    ./gradlew build --dry-run

COPY --link --chown=$uid . .


FROM --platform=$BUILDPLATFORM base_builder AS unfailing-build

RUN --mount=type=cache,gid=$gid,uid=$uid,target=$work_dir/.gradle \
    --mount=type=cache,gid=$gid,uid=$uid,target=$gradle_cache_dir \
    ./gradlew build || (status=$?; mkdir -p build && echo $status > build/failed)


FROM --platform=$BUILDPLATFORM scratch AS build-output
ARG work_dir

COPY --link --from=unfailing-build $work_dir/build .


FROM --platform=$BUILDPLATFORM base_builder AS builder
RUN --mount=type=cache,gid=$gid,uid=$uid,target=$work_dir/.gradle \
    --mount=type=cache,gid=$gid,uid=$uid,target=$gradle_cache_dir \
    --network=none \
    ./gradlew --offline build


FROM --platform=$BUILDPLATFORM scratch
ARG work_dir

COPY --link --from=builder $work_dir/build .
