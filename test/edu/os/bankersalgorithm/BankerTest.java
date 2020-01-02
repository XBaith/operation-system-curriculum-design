package edu.os.bankersalgorithm;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BankerTest {

    @org.junit.Test
    public void test () {
        ArrayList<Process> ps = new ArrayList<>();
        Map<String, Integer> ava = new HashMap<>();
        ava.put("A", 3);ava.put("B", 3);ava.put("C", 2);

        //P0
        Map<String, Integer> max0 =  new HashMap<>();
        max0.put("A", 7);max0.put("B", 5);max0.put("C", 3);
        Map<String, Integer> allocation0 =  new HashMap<>();
        allocation0.put("A", 0);allocation0.put("B", 1);allocation0.put("C", 0);
        ps.add(new Process("P0",max0, allocation0));

        //P1
        Map<String, Integer> max1 =  new HashMap<>();
        max1.put("A", 3);max1.put("B", 2);max1.put("C", 2);
        Map<String, Integer> allocation1 =  new HashMap<>();
        allocation1.put("A", 2);allocation1.put("B", 0);allocation1.put("C", 0);
        ps.add(new Process("P1",max1, allocation1));

        //P2
        Map<String, Integer> max2 =  new HashMap<>();
        max2.put("A", 9);max2.put("B", 0);max2.put("C", 2);
        Map<String, Integer> allocation2 =  new HashMap<>();
        allocation2.put("A", 3);allocation2.put("B", 0);allocation2.put("C", 2);
        ps.add(new Process("P2",max2, allocation2));

        //P3
        Map<String, Integer> max3 =  new HashMap<>();
        max3.put("A", 2);max3.put("B", 2);max3.put("C", 2);
        Map<String, Integer> allocation3 =  new HashMap<>();
        allocation3.put("A", 2);allocation3.put("B", 1);allocation3.put("C", 1);
        ps.add(new Process("P3",max3, allocation3));

        //P4
        Map<String, Integer> max4 =  new HashMap<>();
        max4.put("A", 4);max4.put("B", 3);max4.put("C", 3);
        Map<String, Integer> allocation4 =  new HashMap<>();
        allocation4.put("A", 0);allocation4.put("B", 0);allocation4.put("C", 2);
        ps.add(new Process("P4",max4, allocation4));

        SafetyState safetyState = new SafetyState(ps, ava);
        safetyState.run();

        //request
        Map<String, Integer> req = new HashMap<>();
        req.put("A", 1);req.put("B", 0);req.put("C", 2);
        safetyState.request("1", req);

        //request
        Map<String, Integer> req1 = new HashMap<>();
        req.put("A", 3);req.put("B", 3);req.put("C", 0);
        safetyState.request("1", req1);
    }
}
