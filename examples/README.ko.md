# Stagefy 예제

[English](README.md)

각 폴더는 독립된 예제이며 `Jenkinsfile`과 그것이 실행하는 YAML로 구성됩니다. Jenkinsfile은 `checkout scm` 후 이 저장소 기준 경로(`examples/<name>/Jenkins.yml`)로 파일을 참조하므로, 이 저장소로 Pipeline 잡을 만들어 실행하거나 폴더를 복사해 경로를 수정하세요.

| 예제 | 내용 |
|------|------|
| [01-basic](01-basic) | `steps` 스테이지, `${env.VAR}`, `env`, step 스타일 호출 `stagefy file:, stage:` |
| [02-composition](02-composition) | `stages`(순차)와 `parallels`(병렬) |
| [03-reuse](03-reuse) | `name from file.yml`, `use` 스텝, `makeStage: false` |
| [04-templates](04-templates) | `template` 선언과 인자 |
| [05-iterated](05-iterated) | 리스트, 맵 리스트(`_name`), `env.VAR` 대상 `iterated` |
| [06-conditional](06-conditional) | `when`, `setEnvFromFile` |
| [07-windows](07-windows) | `bat`, `powershell`, `pwsh` |
| [08-advanced](08-advanced) | `script`, `evaluate` (샌드박스 밖에서 Groovy 실행, 신뢰할 수 있는 소스만) |

모든 스테이지 YAML은 [`schema/stagefy.schema.json`](../schema/stagefy.schema.json)을 따르며 에디터 검증용 `yaml-language-server` modeline을 포함합니다.
