package io.jenkins.plugins.stagefy.core

/** Step for YAML key {@code stash}: a map of stash options ({@code name} required) or just the stash name. */
class StashStep extends Step {
    private static final List<String> OPTIONS = ["name", "includes", "excludes", "allowEmpty", "useDefaultExcludes"]

    StashStep(Map raw, def jenkins, Stage stage) {
        super(raw, jenkins, stage)
    }

    void run() {
        def value = raw.get("stash")
        Map args = value instanceof Map ? new LinkedHashMap(value) : [name: value]
        for (String key in args.keySet()) {
            if (!OPTIONS.contains(key)) {
                jenkins.error("Unknown stash option '${key}' in stage '${stage.stagename}'")
            }
        }
        if (!args.get("name")) {
            jenkins.error("stash requires 'name' in stage '${stage.stagename}'")
        }
        jenkins.stash(args)
    }
}
