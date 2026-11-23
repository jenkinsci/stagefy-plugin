package io.jenkins.plugins.stagefy.core

/** Step for YAML key {@code unstash}: restores the stash with the given exact name. */
class UnstashStep extends Step {
    UnstashStep(Map raw, def jenkins, Stage stage) {
        super(raw, jenkins, stage)
    }

    void run() {
        def name = raw.get("unstash")
        if (!(name instanceof CharSequence) || !name.toString().trim()) {
            jenkins.error("unstash requires a name in stage '${stage.stagename}'")
        }
        jenkins.unstash(name.toString())
    }
}
