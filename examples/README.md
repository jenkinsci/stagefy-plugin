# Stagefy examples

[한국어](README.ko.md)

Each folder is a self-contained example: a `Jenkinsfile` and the YAML it runs. The Jenkinsfiles call `checkout scm` and reference files by their path in this repository (`examples/<name>/Jenkins.yml`), so create a Pipeline job from this repo to try one, or copy the folder and adjust the paths.

| Example | Shows |
|---------|-------|
| [01-basic](01-basic) | `steps` stage, `${env.VAR}`, `env`, the step-style `stagefy file:, stage:` call |
| [02-composition](02-composition) | `stages` (sequential) and `parallels` |
| [03-reuse](03-reuse) | `name from file.yml`, `use` step, `makeStage: false` |
| [04-templates](04-templates) | `template` declaration and arguments |
| [05-iterated](05-iterated) | `iterated` over a list, a list of maps (`_name`) and `env.VAR` |
| [06-conditional](06-conditional) | `when`, `setEnvFromFile` |
| [07-windows](07-windows) | `bat`, `powershell`, `pwsh` |
| [08-advanced](08-advanced) | `script`, `evaluate` (runs Groovy outside the sandbox, trusted sources only) |

All stage YAML files follow [`schema/stagefy.schema.json`](../schema/stagefy.schema.json) and carry the `yaml-language-server` modeline for editor validation.
