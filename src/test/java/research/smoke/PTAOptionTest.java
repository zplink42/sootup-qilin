package research.smoke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import qilin.driver.PTAOption;
import qilin.pta.PTAConfig;

public class PTAOptionTest {

  @Before
  public void resetBeforeTest() {
    PTAConfig.reset();
  }

  @After
  public void resetAfterTest() {
    PTAConfig.reset();
  }

  @Test
  public void includeAllOptionActivatesApplicationConfiguration() {
    new PTAOption().parseCommandLine(new String[] {"-includeall"});

    assertTrue(PTAConfig.v().getAppConfig().INCLUDE_ALL);
  }

  @Test
  public void outputDefaultsToWorkspaceOutputDirectory() {
    assertEquals("output", PTAConfig.v().getOutConfig().outDir);
  }
}
