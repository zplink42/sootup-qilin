# DaCapo 2006 SootUp-Qilin Validation

Date: 2026-05-25

## Configuration

The locally copied DaCapo inputs originate from:

```text
D:\gitdesk\qilin-generics\artifact\benchmarks\dacapo2006
```

The validation reproduces the prior artifact's Zipper-style baseline options:

```text
-pae -pe -clinit=ONFLY -lcs -mh
```

Each run uses its benchmark jar, dependency jar, Tamiflex reflection log, and:

```text
D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45
```

The Gradle entry point is:

```powershell
.\gradlew.bat runDacapo2006 -Pbenchmark=antlr -Ppta=insens -PexperimentOptions=true -PdumpStats=true -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45'
```

## Initial Context-Insensitive Baseline

All nine DaCapo 2006 programs mapped in the original `bm2006.py` complete in
the standalone SootUp-Qilin workspace with an 8 GB analysis heap. These
measurements were collected before the inherited-field resolution fix
described below.

| Benchmark | Reachable methods (CI) | Call edges (CI) | Elapsed time | Peak reported memory |
| --- | ---: | ---: | ---: | ---: |
| antlr | 7,369 | 36,852 | 11.53 s | 2,778.34 MB |
| bloat | 9,194 | 61,459 | 19.25 s | 2,983.89 MB |
| chart | 15,572 | 79,724 | 31.89 s | 3,272.19 MB |
| eclipse | 18,823 | 120,793 | 72.22 s | 4,624.05 MB |
| fop | 7,880 | 36,608 | 12.56 s | 3,132.11 MB |
| luindex | 7,281 | 35,624 | 11.19 s | 2,652.20 MB |
| lusearch | 7,874 | 38,276 | 12.11 s | 2,793.86 MB |
| pmd | 11,816 | 63,399 | 23.58 s | 2,968.40 MB |
| xalan | 9,985 | 50,072 | 17.15 s | 3,054.22 MB |

## Context-Sensitive Probes

Before the inherited-field resolution fix, the `antlr` benchmark also
completed for representative algorithm paths:

| PTA | Reachable methods (CI projection) | Call edges (CI projection) | Elapsed time | Peak reported memory |
| --- | ---: | ---: | ---: | ---: |
| `1o` | 7,222 | 35,187 | 22.86 s | 3,063.31 MB |
| `2o` | 6,934 | 30,947 | 31.58 s | 4,664.13 MB |
| `Z-2o` | 6,962 | 31,034 | 39.55 s | 4,340.97 MB |

## Soot Differential Investigation

The `antlr` inputs in `qilin-generics` and this workspace have matching
SHA-256 digests for the application jar, dependency jar, reflection log, and
JRE jars. Running byte-identical inputs and equivalent `insens` options
isolated a SootUp-Qilin propagation defect:

| Configuration | Qilin-Soot (qilin-generics) | SootUp before fix | SootUp after fix |
| --- | ---: | ---: | ---: |
| No reflection log: reachable methods | 7,505 | 7,254 | 7,492 |
| No reflection log: call edges | 42,517 | 36,047 | 42,222 |
| Tamiflex log: reachable methods | 8,194 | 7,369 | 8,180 |
| Tamiflex log: call edges | 57,461 | 36,852 | 57,152 |

The missing path is visible in the ANTLR parser hierarchy. Bytecode in
`antlr.Parser.setTokenBuffer` stores through field
`<antlr.Parser: antlr.ParserSharedInputState inputState>`, whereas bytecode in
`antlr.LLkParser.LA/LT` reads the inherited field through the symbolic
subclass-qualified signature
`<antlr.LLkParser: antlr.ParserSharedInputState inputState>`. The previous
SootUp Qilin port created a synthetic field when direct lookup of the latter
signature failed, separating stores and loads in the PAG. As a result,
`TokenBuffer.fill()` and `ANTLRLexer.nextToken()` were not reachable and
reflection-driven code-generation paths were greatly under-expanded.

`MethodNodeFactory` now resolves field references along their class and
interface hierarchy, normalizing inherited symbolic references to the actual
declaring field. The remaining `antlr` difference is small and mostly consists
of FakeMain naming, Soot/SootUp signature printing, and a handful of boundary
library methods.

## Timing Investigation

The reported `Time (sec)` values are not directly comparable across the two
implementations. Both evaluators time `pta.run()`, but `qilin-generics`
executes `preLoadBodyForFairCompare()` before that timer starts. It retrieves
active Soot bodies in parallel for loaded application and library classes.
SootUp creates a lazy `JavaView` and calls `PTAUtils.getMethodBody()` while
reachable methods are processed inside the timed analysis.

The following runs use the same `antlr` jars, reflection log, legacy JRE, and
`insens` options after the inherited-field fix:

| Run path | Reported PTA time | `Main.run()` wall time | Reachable methods / call edges |
| --- | ---: | ---: | ---: |
| Qilin-Soot default, run 1 | 3.955 s | 17.573 s | 8,194 / 57,461 |
| Qilin-Soot default, run 2 | 4.338 s | 17.738 s | 8,194 / 57,461 |
| SootUp lazy bodies, run 1 | 12.338 s | 15.470 s | 8,180 / 57,152 |
| SootUp lazy bodies, run 2 | 12.220 s | 15.371 s | 8,180 / 57,152 |
| SootUp, preloaded reachable concrete bodies | 4.700 s | 8.411 s preload before timer | 8,180 / 57,152 |
| SootUp, preloaded all concrete bodies | 4.237 s | 122.541 s preload before timer | 8,180 / 57,152 |

Preloading the `7,994` reachable concrete bodies reduces SootUp's timed PTA
work by about `7.5 s` without changing its graph. This isolates lazy bytecode
body conversion and caching as the main cause of the apparent `3 s` versus
`10+ s` difference. Preloading all `145,458` concrete bodies is not a usable
optimization: it reduces the reported number only by performing substantially
more work outside the timer. For performance comparisons, use an end-to-end
timer or report front-end body-loading and propagation time separately.

## Findings

1. SootUp-Qilin `v2.0.0` can execute real DaCapo-scale pointer analyses in
   this standalone jar-backed arrangement; it is not limited to smoke tests.
2. The runtime model remains tied to legacy JRE jars. `PTAUtils.getJreJars`
   only loads `<jre>/lib/*.jar`, so a Java 9+ modular runtime is not currently
   a drop-in replacement for `jre1.6.0_45`.
3. Tamiflex processing is incomplete for two completed runs. `eclipse` logs
   unresolved Xalan target classes, and `fop` logs unresolved Xerces target
   classes. The classes are absent from those benchmarks' configured
   dependency jars but present in other copied DaCapo jars, so faithful
   reflection coverage needs a corrected dependency closure or revised logs.
4. Context-sensitive analyses run, but the enabled `SimplifiedEvaluator`
   prints only CI-projected metrics. Measuring improvements to a new
   context-sensitive algorithm will require additional CS-oriented metrics or
   result exports.
5. A correctness defect in inherited-field resolution is fixed locally.
   Without this change, SootUp Qilin can materially under-approximate
   points-to propagation and call-graph expansion even for `insens`.
6. The higher SootUp `Time (sec)` for `antlr` is primarily a timing-boundary
   artifact: SootUp includes lazy body construction, while `qilin-generics`
   performs Soot body preloading before its evaluator timer. In end-to-end
   runs collected here, SootUp is slightly faster.
7. Two upstream usability defects found during validation are fixed in this
   workspace: output now defaults below `output/` instead of the filesystem
   root, and `-includeall` is no longer ignored due to a spelling mismatch.

## Post-Fix Regression Checks

The following commands pass after the local fixes:

```powershell
.\gradlew.bat --no-daemon clean test -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45'
.\gradlew.bat --no-daemon upstreamTest --tests qilin.test.core.FieldTests -PqilinJre='D:\gitdesk\sootup\benchmarks\JREs\jre1.6.0_45'
.\gradlew.bat --no-daemon upstreamTest --tests qilin.test.core.AssignTests.testSimpleAssign -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45'
.\gradlew.bat --no-daemon upstreamTest --tests qilin.test.core.ReflogTests.testClassForName -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45'
.\gradlew.bat --no-daemon runDacapo2006 -Pbenchmark=antlr -Ppta=insens -PexperimentOptions=true -PdumpStats=true -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45' -PqilinHeap=8g
```
