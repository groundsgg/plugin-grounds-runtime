# plugin-grounds-runtime

Shared runtime library provider plugins for Grounds Paper and Velocity workloads.

The Paper and Velocity artifacts expose selected shared JVM libraries for internal Grounds plugins that use the runtime-consumer Gradle conventions.

## Runtime platform artifacts

This repository owns the shared runtime platform:

- `plugin-grounds-runtime-paper`
- `plugin-grounds-runtime-velocity`
- `grounds-runtime-bom`
- `grounds-runtime-catalog`

`runtime-catalog/grounds-runtime-libraries.json` is the source of truth for shared runtime library versions. The BOM and runtime diagnostics are generated from that catalog.

## Adding or updating a shared dependency

Use this flow when a library should be provided by `plugin-grounds-runtime` instead of bundled into each consumer plugin.

1. Decide whether the library is actually shared runtime surface.

   Add it only when multiple internal Paper or Velocity plugins need the same library at runtime and all target environments install `plugin-grounds-runtime`. Keep plugin-private clients, app-specific libraries, and test-only libraries in consumer plugin builds.

2. Update `runtime-catalog/grounds-runtime-libraries.json`.

   Add or change the Maven `group`, `name`, and `version`. This updates the published `grounds-runtime-bom`, the runtime catalog artifact, and `/grounds-runtime` diagnostics.

3. Update provider shading when needed.

   If the dependency exposes packages that can conflict with server or plugin classpaths, add relocations in the runtime catalog fields used by the convention plugin and keep Paper and Velocity provider relocation behavior aligned.

4. Update `library-gradle-plugin` only when the module set changes.

   Version-only changes stay in this repository. Additions or removals also need the versionless module list, relocation policy, shadow exclusions, and forbidden package prefixes updated in `library-gradle-plugin`.

5. Verify locally.

   Run `./gradlew test`, `./gradlew spotlessApply`, and `./gradlew build`. For consumer behavior, publish this runtime platform and the matching convention plugin to Maven Local, then build a real runtime-consumer plugin.

6. Release in order.

   Merge and release `plugin-grounds-runtime` first. Then release `library-gradle-plugin` if its convention policy changed. Finally bump consumer plugin `groundsRuntime.version` and container `GROUNDS_RUNTIME_PLUGIN_VERSION` to the released runtime version.
