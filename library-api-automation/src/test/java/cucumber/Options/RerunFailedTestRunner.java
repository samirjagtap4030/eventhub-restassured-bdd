package cucumber.Options;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "@target/failed_scenarios.txt",
    glue = {"stepDefinations"},
    plugin = {"json:target/jsonReports/cucumber-rerun-report.json"}
)
public class RerunFailedTestRunner {
}
