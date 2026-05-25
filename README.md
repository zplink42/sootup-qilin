# SootUp Qilin Research Workspace

This repository is a standalone editable copy of the Qilin module from
`soot-oss/SootUp` tag `v2.0.0` (commit `cbcc0ee56455f44ecadf75c2fb6363810ffb6126`).
`2.0.0` is the latest released `org.soot-oss:sootup.qilin` version available
from Maven Central when this workspace was initialized on 2026-05-25.

## Layout

- `src/main/java/qilin`: upstream Qilin implementation, intended for local research changes.
- `src/test/java/qilin/microben`: upstream pointer-analysis microbenchmarks.
- `src/test/java/qilin/test`: upstream test harness, available through the optional
  `upstreamTest` task because it requires a legacy JRE model.
- `src/test/java/research/smoke`: small local verification test.

Only Qilin source is vendored. SootUp framework layers remain external jar
dependencies resolved by Gradle:

```groovy
api "org.soot-oss:sootup.java.bytecode.frontend:2.0.0"
api "org.soot-oss:sootup.callgraph:2.0.0"
```

Their transitive dependencies provide `sootup.core`, `sootup.java.core`, and
the other API types used by Qilin.

## Build

The project runs on the installed JDK 21, while Qilin source is compiled with
Java 8-compatible bytecode to match the released upstream module. Analysis
tests run with a 4 GB maximum heap, matching the memory scale of the upstream
Qilin test configuration when runtime jars are loaded.

```powershell
.\gradlew.bat clean classes
```

Qilin `v2.0.0` models the runtime from JRE jar files rather than the Java 9+
module image. Supply a Java 8-or-earlier JRE directory when executing analyses
or the smoke test:

```powershell
.\gradlew.bat test -PqilinJre='D:\gitdesk\qilin-equiv\artifact\benchmarks\JREs\jre1.6.0_45'
```

To run a copied upstream microbenchmark regression:

```powershell
.\gradlew.bat upstreamTest --tests qilin.test.core.AssignTests.testSimpleAssign -PqilinJre='D:\gitdesk\qilin-equiv\artifact\benchmarks\JREs\jre1.6.0_45'
```

The task also accepts broader test filters, for example
`--tests qilin.test.core.AssignTests`. Running the entire imported upstream
suite in one invocation is available by omitting `--tests`, but is not a green
baseline for this release: on this workstation it reaches the collection and
context-sensitive tests before exhausting a 4 GB test JVM. Prefer targeted
microbenchmark groups while developing an analysis.

## Run Qilin

Build an application/classes directory or jar and provide it as `-apppath`.
For a single explicit entry point, `-singleentry` avoids Qilin's broader
implicit runtime entry construction:

```powershell
.\gradlew.bat run --args='-pta=insens -singleentry -apppath=<classes-or-jar> -mainclass=<fully.qualified.Main> -jre=<legacy-jre-root>'
```

Useful PTA patterns include `insens`, `1c`, `2o`, `2t`, `Z-2o`, and `T-2o`.

## DaCapo 2006 Validation

The local `benchmarks/dacapo2006` directory contains the inputs copied from
`D:\gitdesk\qilin-generics\artifact\benchmarks\dacapo2006`. Its jar files and
Tamiflex logs are ignored by Git so experiments remain local without making
this research repository carry 87 MB of binary fixtures.

The `runDacapo2006` task reproduces the benchmark-to-entry-point, dependency,
reflection-log, and JRE conventions from the prior Qilin artifact. It covers
the nine DaCapo programs declared in its `bm2006.py`:
`antlr`, `bloat`, `chart`, `eclipse`, `fop`, `luindex`, `lusearch`, `pmd`,
and `xalan`.

Run a context-insensitive baseline using the same precision flags used by the
older Zipper-style artifact configuration:

```powershell
.\gradlew.bat runDacapo2006 -Pbenchmark=antlr -Ppta=insens -PexperimentOptions=true -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45'
```

The task defaults to an 8 GB analysis JVM. Override it with
`-PqilinHeap=12g`, enable lightweight single-entry mode with
`-PsingleEntry=true`, or request Qilin result files with `-PdumpStats=true`.
Generated results are placed under the ignored local `output` directory.

Detailed results and current analysis limitations are recorded in
`docs/DACAPO2006-VALIDATION.md`.

## Research Workflow

Make algorithm changes under `src/main/java/qilin`, especially:

- `qilin/pta/tools` for analysis variants.
- `qilin/parm` for context and heap abstraction policies.
- `qilin/core/pag` and `qilin/core/solver` for propagation changes.

Keep the `sootupVersion` value pinned while running an experiment campaign.
Upgrading SootUp may require source adaptations because Qilin's SootUp-facing
API is still evolving upstream.

One local compatibility adjustment is already applied: `OnFlyCallGraph`
retains its public `getCalls()` method without `@Override`, because the
published `sootup.callgraph:2.0.0` jar omits that interface method even though
the `v2.0.0` repository source declares it.

Two small runtime fixes are also applied locally: dumped result files now
default to `output` rather than the filesystem root, and the `-includeall`
command-line option now activates its declared configuration flag.

Verification performed while creating this workspace:

- `clean test` with the existing `jre1.6.0_45` model passes.
- `upstreamTest --tests qilin.test.core.AssignTests.testSimpleAssign` passes.
- Unfiltered `upstreamTest` is retained for investigation, but currently runs
  out of memory during later test groups as described above.

## Provenance

The copied sources retain their upstream LGPL headers. The upstream repository
is <https://github.com/soot-oss/SootUp>, and the imported module originates
from `sootup.qilin` at tag `v2.0.0`.
