package io.jenkins.plugins.stagefy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import hudson.Functions;
import hudson.model.Result;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/** Runs the Jenkinsfiles under examples/ so they stay in sync with the plugin. */
@WithJenkins
class ExamplesTest {

    private static final Path EXAMPLES = Paths.get("examples").toAbsolutePath();

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
        assumeFalse(Functions.isWindows(), "examples rely on a POSIX shell");
    }

    /** Runs examples/&lt;name&gt;/Jenkinsfile, with `checkout scm` replaced by copying examples/ into the workspace. */
    private WorkflowRun run(String name) throws Exception {
        String script = Files.readString(EXAMPLES.resolve(name).resolve("Jenkinsfile"), StandardCharsets.UTF_8)
                .replace("checkout scm", "sh 'cp -r \"" + EXAMPLES + "\" .'");
        WorkflowJob job = j.createProject(WorkflowJob.class);
        job.setDefinition(new CpsFlowDefinition(script, true));
        return j.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
    }

    private void assertLog(WorkflowRun run, String text) throws Exception {
        assertTrue(JenkinsRule.getLog(run).contains(text), "log should contain: " + text);
    }

    @Test
    void basic() throws Exception {
        assertLog(run("01-basic"), "Hello from Stagefy");
    }

    @Test
    void composition() throws Exception {
        WorkflowRun run = run("02-composition");
        assertLog(run, "running unit tests");
        assertLog(run, "running lint");
    }

    @Test
    void reuse() throws Exception {
        WorkflowRun run = run("03-reuse");
        assertLog(run, "checking out sources");
        assertLog(run, "notifying the team");
    }

    @Test
    void templates() throws Exception {
        WorkflowRun run = run("04-templates");
        assertLog(run, "deploying 1.2.0 to staging");
        assertLog(run, "deploying 1.2.0 to production");
    }

    @Test
    void iterated() throws Exception {
        WorkflowRun run = run("05-iterated");
        assertLog(run, "smoke test staging");
        assertLog(run, "deploy to prod with 5 replicas");
        assertLog(run, "smoke test tokyo");
    }

    @Test
    void conditional() throws Exception {
        WorkflowRun run = run("06-conditional");
        assertLog(run, "building for staging");
        assertTrue(!JenkinsRule.getLog(run).contains("deploying to"), "deploy must be skipped by default");
    }

    @Test
    void advanced() throws Exception {
        WorkflowRun run = run("08-advanced");
        assertLog(run, "Hello from a Groovy script");
        assertLog(run, "evaluated at");
    }
}
