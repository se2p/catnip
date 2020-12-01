package de.uni_passau.fim.se2.catnip.recommendation;

import com.opencsv.exceptions.CsvException;
import de.uni_passau.fim.se2.catnip.recommendation.TargetSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TargetSelectorTest {

    @Test
    public void test100Percent() throws IOException, CsvException {
        TargetSelector targetSelector = new TargetSelector(100);
        List<String> targets = targetSelector.getViableTargetNames("./src/test/fixtures/testWhiskerResults.csv");
        Assertions.assertEquals(4,targets.size());
        List<String> allTargets = new ArrayList<>();
        allTargets.add("5f04b2e9449ae_BootsrennenFertig-11");
        allTargets.add("5f05a5ddd1fc0_Bootsrennen-2-4");
        allTargets.add("5f05a8c027065_1-Aufgabe");
        allTargets.add("5f05bbb675bb3_Bootsrennen-fertig-1-1");
        Assertions.assertEquals(allTargets, targets);
    }

    @Test
    public void testStandardPercent() throws IOException, CsvException {
        TargetSelector targetSelector = new TargetSelector();
        List<String> targets = targetSelector.getViableTargetNames("./src/test/fixtures/testWhiskerResults.csv");
        Assertions.assertEquals(6,targets.size());
        List<String> allTargets = new ArrayList<>();
        allTargets.add("5f042d99df0b6_Bootsrennen_Fertig");
        allTargets.add("5f043f15e6880_Bootsrennen-fertig-3");
        allTargets.add("5f04b2e9449ae_BootsrennenFertig-11");
        allTargets.add("5f05a5ddd1fc0_Bootsrennen-2-4");
        allTargets.add("5f05a8c027065_1-Aufgabe");
        allTargets.add("5f05bbb675bb3_Bootsrennen-fertig-1-1");
        Assertions.assertEquals(allTargets, targets);
    }
}
