package ptest.com.g414.st9.proto.service.perf;

import static com.sun.faban.driver.CycleType.THINKTIME;

import java.util.concurrent.TimeUnit;

import com.google.inject.Key;
import com.sun.faban.driver.BenchmarkDefinition;
import com.sun.faban.driver.BenchmarkDriver;
import com.sun.faban.driver.BenchmarkOperation;
import com.sun.faban.driver.FlatMix;
import com.sun.faban.driver.NegativeExponential;

@BenchmarkDefinition(name = "BasicDriverScenario01", version = "1.0")
@BenchmarkDriver(name = "BasicDriverScenario01", responseTimeUnit = TimeUnit.MICROSECONDS)
@FlatMix(operations = { "create", "retrieve" }, mix = { 0.5, 0.5 })
public class BasicDriverScenario01 extends BasicDriverScenarioBase {
    public BasicDriverScenario01() throws Exception {
        super();
    }

    @BenchmarkOperation(name = "create", max90th = 100000)
    @NegativeExponential(cycleType = THINKTIME, cycleMean = 0, cycleDeviation = 0.0)
    public void create() throws Exception {
        super.create();
    }

    @BenchmarkOperation(name = "retrieve", max90th = 100000)
    @NegativeExponential(cycleType = THINKTIME, cycleMean = 0, cycleDeviation = 0.0)
    public void retrieve() throws Exception {
        super.retrieve();
    }

    public static class GuiceModule extends BasicDriverScenarioBase.GuiceModule {
        @Override
        protected void configure() {
            super.configure();

            bind(Key.get(Object.class, BenchmarkDriver.class)).to(
                    BasicDriverScenario01.class);
        }
    }
}