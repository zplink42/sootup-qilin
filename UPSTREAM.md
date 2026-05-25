# Upstream Import Record

- Repository: `https://github.com/soot-oss/SootUp`
- Module: `sootup.qilin`
- Imported tag: `v2.0.0`
- Tag commit: `cbcc0ee56455f44ecadf75c2fb6363810ffb6126`
- Import date: `2026-05-25`

Imported paths:

- `sootup.qilin/src/main/java` -> `src/main/java`
- `sootup.qilin/src/test/java` -> `src/test/java`
- root `LICENSE` -> `LICENSE`

The upstream module POM is intentionally not copied as the active build. This
standalone project uses Gradle so that the Qilin sources remain local while
SootUp framework components resolve as released jars from Maven Central.

Local compatibility patch:

- `src/main/java/qilin/core/builder/callgraph/OnFlyCallGraph.java`: removed
  `@Override` from `getCalls()`. The `v2.0.0` tag source declares this method
  on `CallGraph`, but the published `sootup.callgraph:2.0.0` jar does not;
  retaining the public method without the annotation keeps Qilin compilable
  against the released jar boundary.
- `src/test/java/qilin/test/util/JunitTests.java`: accepts Gradle-provided test
  classes, reflection-log, and legacy-JRE locations instead of requiring the
  original multi-module repository layout.
- `src/main/java/qilin/CoreConfig.java`: defaults generated result files to
  local `output/` instead of an absolute filesystem-root path.
- `src/main/java/qilin/driver/PTAOption.java`: fixes the misspelled
  `-includeall` option lookup so the declared flag is effective.

Validation baseline at import time:

- Local smoke analysis: passing with
  `D:\gitdesk\qilin-equiv\artifact\benchmarks\JREs\jre1.6.0_45`.
- Upstream `qilin.test.core.AssignTests.testSimpleAssign`: passing with the
  same runtime model.
- Full unfiltered `upstreamTest`: reaches 25 tests and then fails from test
  worker memory exhaustion in later collection/context groups under a 4 GB
  heap; it is preserved as an investigatory suite rather than the default
  acceptance gate.
- Imported DaCapo 2006 validation: all nine programs configured in the old
  Qilin artifact complete under `insens` with an 8 GB analysis heap; `antlr`
  additionally completes under `1o`, `2o`, and `Z-2o`. See
  `docs/DACAPO2006-VALIDATION.md`.
