package StorageModule.test.exceptionhandling;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All Exception Handling Tests")
@SelectClasses({
    MultipleExceptionHandlerTest.class,
    ExceptionRethrowerTest.class,
    ResourceManagerTest.class
})
public class AllExceptionHandlingTests {
}