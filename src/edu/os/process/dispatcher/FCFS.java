package edu.os.process.dispatcher;

import edu.os.process.ProcessScheduling;

import java.util.Comparator;

/**
 * 先到先服务
 * 到达时间在前，排序在前
 */
public class FCFS implements Comparator<ProcessScheduling.Process> {
    @Override
    public int compare(ProcessScheduling.Process p1, ProcessScheduling.Process p2) {
        return p1.arrivalTime < p2.arrivalTime ?
                ((p1.arrivalTime == p2.arrivalTime) ? 0 : -1)
                : 1;
    }
}
