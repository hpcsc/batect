# Golang

You can see a full example of using batect with Golang in [the Golang sample project](https://github.com/batect/batect-sample-golang).

## Example configuration

```yaml
containers:
  build-env:
    image: golang:1.14.0-stretch
    volumes:
      - local: .
        container: /code
        options: cached
      - type: cache
        name: go-cache
        container: /go
    working_directory: /code
    environment:
      # With the image above, GOPATH defaults to /go, so we don't need to set it explicitly.
      GOCACHE: /go/cache
```

## Caching dependencies

!!! tip "tl;dr"
    Mount a cache into your container for your `GOPATH` and `GOCACHE`, otherwise you'll have to download and compile your dependencies every
    time the build runs

### `GOPATH`

Golang caches the source for dependencies under your [`GOPATH`](https://github.com/golang/go/wiki/GOPATH). By default, this is at `$HOME/go`.
However, because batect destroys all of your containers once the task finishes, this directory is lost at the end of every task run - which means that Golang
will have to download all of your dependencies again next time you run the task, significantly slowing down the build.

The solution to this is to mount a [cache](../tips/Performance.md#cache-volumes) that persists between builds into your container for your `GOPATH`.

For example, the [official Golang Docker images](https://hub.docker.com/_/golang) set `GOPATH` to `/go`, so mounting a cache at `/go` inside the container will
allow your dependencies to be persisted across builds.

### `GOCACHE`

The Golang compiler caches intermediate build output (such as built libraries) in [`GOCACHE`](https://golang.org/cmd/go/#hdr-Build_and_test_caching).
Just like for `GOPATH`, the contents of `GOCACHE` will be lost when the task finishes and the container is removed, which means that Golang will have to recompile
all code for your project, even if it has not changed. This can also significantly slow down the build.

Again, the solution is to mount a [cache](../tips/Performance.md#cache-volumes) that persists between builds into your container for your `GOCACHE`.

The [official Golang Docker images](https://hub.docker.com/_/golang) do not set a default for `GOCACHE`, so you will need to set this yourself. In the example above, `GOCACHE` has been placed
inside `/go` (which is the default `GOPATH`) so that both use the same cache.
