# LiveWire

Unity-style `[SerializeField]` for Java: annotate a field with `@ExposedConfig`
and edit its value at runtime from a JSON file or a built-in web UI, with no
server restart.

## Purpose

Game servers, mods, and long-running JVM processes typically force a full
restart to tweak a tuning knob — drop rates, movement speed, difficulty
multipliers. LiveWire closes that gap. It gives Java the same "change a value
in the inspector and see it instantly" loop that Unity gives C#, without
asking the developer to write load/save plumbing for every field.

The pipeline is the one Unity hides behind its inspector, reassembled out of
plain JDK parts:

1. **Annotation** — `@ExposedConfig` marks a field as live-editable
   (`RetentionPolicy.RUNTIME`, `ElementType.FIELD`).
2. **Reflection** — at registration, LiveWire scans the class (including
   superclasses), finds annotated fields, calls `setAccessible(true)`, and
   stores a `ConfigBinding` for each one.
3. **File watcher** — `java.nio.file.WatchService` watches the JSON config
   file; on save, values are parsed via Jackson and written back into the
   live objects through the stored `Field` references.
4. **Web UI** — a tiny embedded HTTP server renders the current bindings
   and accepts edits, which flow through the same apply path.

## What's in the box

- `com.livewire.config.ExposedConfig` — the annotation (`key`, `description`).
- `com.livewire.config.ConfigRegistry` — reflection scan, binding storage,
  change listeners. Handles both instance fields (`register(obj)`) and static
  fields (`registerStatic(Class)`).
- `com.livewire.config.ConfigStore` — JSON read/write via Jackson; the file
  is authored from the registry on first run so disk and code stay in sync.
- `com.livewire.config.ConfigWatcher` — `WatchService` loop that ignores
  writes LiveWire itself made (so the web UI doesn't trigger a self-reload
  storm) but still picks up external editor saves.
- `com.livewire.config.ConfigWebServer` — server-rendered HTML form + JSON
  endpoints for inspecting and updating values from the browser.
- `com.livewire.config.LiveConfig` — façade that wires the four together
  and exposes `register`, `registerStatic`, `start`, `close`.
- `com.livewire.demo.GameSettings` / `Demo` — a runnable example exposing
  ints, doubles, booleans, strings, an enum, and a static field.

## Install

Published via [JitPack](https://jitpack.io). Add the JitPack repository and
the `livewire-core` artifact to your build.

**Maven**

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.LucaStudiosGit.LiveWire</groupId>
  <artifactId>livewire-core</artifactId>
  <version>0.1.0</version>
</dependency>
```

**Gradle**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.LucaStudiosGit.LiveWire:livewire-core:0.1.0'
}
```

Replace `0.1.0` with any released tag, a branch name (`main-SNAPSHOT`), or a
commit hash — JitPack will build and cache it on first request.

## Usage

```java
GameSettings settings = new GameSettings();

LiveConfig config = new LiveConfig(Path.of("livewire-config.json"), 7777);
config.register(settings);                  // instance fields
config.registerStatic(GameSettings.class);  // static fields
config.start();
```

After `start()`:

- `livewire-config.json` is created (or merged) next to the process.
- The web UI is available at `http://localhost:7777/`.
- Editing either source updates the live objects in place; registered change
  listeners (`registry().addChangeListener(...)`) fire on every applied write.

## Supported types

Whatever Jackson can deserialize into the declared field type — primitives
and their boxed equivalents, `String`, enums, and POJOs/records. The
deserialized value is assigned through the stored `Field`, so the field's
static type drives the conversion.

## Caveats

- Hot-swapping a field is a plain `Field.set`. Concurrent readers see the
  new value on their next read, but LiveWire does not coordinate with
  surrounding logic — if a value participates in a multi-step calculation,
  the caller is responsible for any locking or snapshotting it needs.
- Duplicate keys (same `@ExposedConfig(key = ...)` or same
  `ClassName.fieldName` across registrations) fail fast at registration.
- The watcher debounces its own writes by path + timestamp; external editors
  that write through a temp file and rename are handled, but very exotic
  save strategies may miss a reload.

## Repository layout

```
LiveWire/
├── pom.xml              parent (packaging=pom)
├── livewire-core/       the published library
└── livewire-demo/       runnable example (not published)
```

Only `livewire-core` is intended as a dependency. `livewire-demo` exists so
contributors can see the end-to-end loop in action.

## Build & run the demo

Requires JDK 17 and Maven.

```
mvn install -DskipTests
mvn -pl livewire-demo exec:java
```

The first command builds the parent + core + demo and installs `livewire-core`
to your local Maven repo. The second runs the demo. Once it prints
`[demo] LiveWire running.`, edit `livewire-config.json` (created next to the
process) or open the web UI at `http://localhost:7777/`. The demo prints the
live field values every three seconds so you can watch them change.

(A single-command `mvn -pl livewire-demo -am exec:java` doesn't work because
`-am` schedules `exec:java` on the parent and core too, where there's no
`mainClass`.)

## Testing

### Automated (JUnit 5)

`livewire-core` ships a focused test suite covering the registry (instance vs
static, custom keys, duplicates, change listeners), bindings (typed
read/write, JSON coercion), the store (round-trip, parent-dir creation),
and `LiveConfig` end-to-end (defaults written, existing file loaded, missing
keys merged, web API updates).

```
mvn -pl livewire-core test
```

Runs in ~4 seconds. No flaky watcher tests are included — the file-watcher
on macOS uses `PollingWatchService` with an interval too long for reliable
unit testing. Manual smoke test below covers that path.

### Manual smoke test from a separate project

Create a throwaway project to confirm the published artifact works end-to-end.

`pom.xml`:

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>example</groupId>
  <artifactId>livewire-smoke</artifactId>
  <version>1.0-SNAPSHOT</version>
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
      <groupId>com.github.LucaStudiosGit.LiveWire</groupId>
      <artifactId>livewire-core</artifactId>
      <version>0.1.0</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.4.1</version>
        <configuration>
          <mainClass>example.Smoke</mainClass>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

`src/main/java/example/Smoke.java`:

```java
package example;

import com.livewire.config.ExposedConfig;
import com.livewire.config.LiveConfig;

import java.nio.file.Path;

public class Smoke {
    @ExposedConfig int score = 0;
    @ExposedConfig String label = "hello";

    public static void main(String[] args) throws Exception {
        Smoke s = new Smoke();
        LiveConfig config = new LiveConfig(Path.of("smoke.json"), 7777);
        config.register(s);
        config.start();

        System.out.println("web UI: http://localhost:7777/");
        while (true) {
            Thread.sleep(2000);
            System.out.printf("score=%d label=%s%n", s.score, s.label);
        }
    }
}
```

Run it:

```
mvn compile exec:java
```

(`exec:java` doesn't compile first, so always pair it with `compile` — or
`package` — when you've changed sources.)

Then exercise the three input paths:

1. **Web UI** — open `http://localhost:7777/`, edit a value, click Apply.
   Next tick should show the new value.
2. **JSON file** — `echo '{"Smoke.score": 42, "Smoke.label": "edited"}' > smoke.json`.
   Within a few seconds the watcher fires and ticks show `score=42 label=edited`.
3. **HTTP API** — `curl -X POST -H 'Content-Type: application/json' \
   -d '{"key":"Smoke.score","value":99}' http://localhost:7777/api/config`.
   Immediate effect on the next tick.

## Releasing

JitPack builds on demand from a Git tag — no manual upload step:

1. Bump `<version>` in the parent `pom.xml` (and let child modules inherit).
2. Tag the commit: `git tag 0.2.0 && git push --tags`.
3. The first time anyone requests
   `com.github.LucaStudiosGit.LiveWire:livewire-core:0.2.0`, JitPack runs the
   build defined in `jitpack.yml` and caches the artifact.
