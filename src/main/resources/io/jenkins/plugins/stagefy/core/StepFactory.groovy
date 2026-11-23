package io.jenkins.plugins.stagefy.core

/** Picks the Step implementation from the directive key of a step map. */
class StepFactory {
    /** Dispatches on sh/bat/powershell/pwsh/script/setEnvFromFile/stash/unstash/evaluate/use; unknown directives call error(). */
    static Step create(Map step, def jenkins, Stage stage) {
        for (String kind in ["sh", "bat", "powershell", "pwsh"]) {
            if (step.containsKey(kind)) return new ShStep(step, jenkins, stage, kind)
        }
        if (step.containsKey("script")) return new ScriptStep(step, jenkins, stage)
        if (step.containsKey("setEnvFromFile")) return new SetEnvFromFileStep(step, jenkins, stage)
        if (step.containsKey("stash")) return new StashStep(step, jenkins, stage)
        if (step.containsKey("unstash")) return new UnstashStep(step, jenkins, stage)
        if (step.containsKey("evaluate")) return new EvaluateStep(step, jenkins, stage)
        if (step.containsKey("use")) return new UseStageStep(step, jenkins, stage)
        jenkins.error("Unknown step directive '${step.keySet().first()}' in stage '${stage.stagename}'")
        return null
    }
}
