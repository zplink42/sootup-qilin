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

## Context-Insensitive Baseline

All nine DaCapo 2006 programs mapped in the original `bm2006.py` complete in
the standalone SootUp-Qilin workspace with an 8 GB analysis heap.

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

The `antlr` benchmark also completes for representative algorithm paths:

| PTA | Reachable methods (CI projection) | Call edges (CI projection) | Elapsed time | Peak reported memory |
| --- | ---: | ---: | ---: | ---: |
| `1o` | 7,222 | 35,187 | 22.86 s | 3,063.31 MB |
| `2o` | 6,934 | 30,947 | 31.58 s | 4,664.13 MB |
| `Z-2o` | 6,962 | 31,034 | 39.55 s | 4,340.97 MB |

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
5. Two upstream usability defects found during validation are fixed in this
   workspace: output now defaults below `output/` instead of the filesystem
   root, and `-includeall` is no longer ignored due to a spelling mismatch.

## Post-Fix Regression Checks

The following commands pass after the local fixes:

```powershell
.\gradlew.bat --no-daemon clean test -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45'
.\gradlew.bat --no-daemon upstreamTest --tests qilin.test.core.AssignTests.testSimpleAssign -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45'
.\gradlew.bat --no-daemon runDacapo2006 -Pbenchmark=antlr -Ppta=insens -PexperimentOptions=true -PdumpStats=true -PqilinJre='D:\gitdesk\qilin-generics\artifact\benchmarks\JREs\jre1.6.0_45' -PqilinHeap=8g
```
