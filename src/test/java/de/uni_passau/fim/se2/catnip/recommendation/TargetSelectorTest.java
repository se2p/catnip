/*
 * Copyright (C) 2019 Catnip contributors
 *
 * This file is part of Catnip.
 *
 * Catnip is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Catnip is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Catnip. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.catnip.recommendation;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TargetSelectorTest {

    @Test
    public void test100Percent() throws IOException, CsvException {
        TargetSelector targetSelector = new TargetSelector(100);
        List<String> targets = targetSelector.getViableTargetNamesByPercentage("./src/test/fixtures/testWhiskerResults.csv", "someSourceName");
        Assertions.assertEquals(4, targets.size());
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
        List<String> targets = targetSelector.getViableTargetNamesByPercentage("./src/test/fixtures/testWhiskerResults.csv", "someSourceName");
        Assertions.assertEquals(7, targets.size());
        List<String> allTargets = new ArrayList<>();
        allTargets.add("5f042d99df0b6_Bootsrennen_Fertig");
        allTargets.add("5f043f15e6880_Bootsrennen-fertig-3");
        allTargets.add("5f04b2e9449ae_BootsrennenFertig-11");
        allTargets.add("5f05a5ddd1fc0_Bootsrennen-2-4");
        allTargets.add("5f05a8c027065_1-Aufgabe");
        allTargets.add("5f05bbb675bb3_Bootsrennen-fertig-1-1");
        allTargets.add("5f044d5d031cc_Bootsrennen-2-3");
        Assertions.assertEquals(allTargets, targets);
    }

    @Test
    public void testIndividual() throws IOException, CsvException {
        TargetSelector targetSelector = new TargetSelector();
        List<String> targets = targetSelector.getViableTargetNamesIndividualBetter("./src/test/fixtures/testWhiskerResults.csv", "5f043f15e6880_Bootsrennen-fertig-3");
        Assertions.assertEquals(4, targets.size());
        List<String> allTargets = new ArrayList<>();
        allTargets.add("5f04b2e9449ae_BootsrennenFertig-11");
        allTargets.add("5f05a5ddd1fc0_Bootsrennen-2-4");
        allTargets.add("5f05a8c027065_1-Aufgabe");
        allTargets.add("5f05bbb675bb3_Bootsrennen-fertig-1-1");
        Assertions.assertEquals(allTargets, targets);
    }

    @Test
    public void testIndividualOther() throws IOException, CsvException {
        TargetSelector targetSelector = new TargetSelector();
        List<String> targets = targetSelector.getViableTargetNamesIndividualBetter("./src/test/fixtures/testWhiskerResults.csv", "5f044d5d031cc_Bootsrennen-2-2");
        Assertions.assertFalse(targets.contains("5f044d5d031cc_Bootsrennen-2-3"));
        Assertions.assertEquals(6, targets.size());
    }
}
