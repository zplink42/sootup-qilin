package research.smoke;

import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import qilin.core.PTA;
import qilin.driver.PTAFactory;
import qilin.driver.PTAPattern;
import qilin.pta.PTAConfig;
import qilin.util.PTAUtils;
import sootup.core.views.View;

public class QilinSmokeTest {

  @After
  public void resetConfig() {
    PTAConfig.reset();
  }

  @Test
  public void runsQilinOnOfficialMicrobenchmark() {
    String legacyJre = System.getProperty("qilin.jre");
    Assume.assumeNotNull(legacyJre);

    String testClasses =
        Paths.get(System.getProperty("qilin.test.classes")).toAbsolutePath().toString();
    String entrypoint = "qilin.microben.core.assign.SimpleAssign";
    PTAPattern pattern = new PTAPattern("insens");

    PTAConfig.v().getAppConfig().APP_PATH = testClasses;
    PTAConfig.v().getAppConfig().JRE = legacyJre;
    PTAConfig.v().getAppConfig().MAIN_CLASS = entrypoint;
    PTAConfig.v().getPtaConfig().singleentry = true;
    PTAConfig.v().getPtaConfig().ptaPattern = pattern;
    PTAConfig.v().getPtaConfig().ptaName = pattern.toString();

    View view = PTAUtils.createView();
    PTA pta = PTAFactory.createPTA(pattern, view, entrypoint);
    pta.run();

    assertFalse(pta.getNakedReachableMethods().isEmpty());
  }
}
