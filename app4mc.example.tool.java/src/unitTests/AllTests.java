package unitTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EndToEndImplicitTest.class, GetRunnablesfromEventChainTest.class, LETlatencyTest.class,
		RunnableMappingForEventChainTest.class })

public class AllTests {

}