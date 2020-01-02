package edu.os.process.dispatcher;

import edu.os.process.ProcessScheduling;

import java.util.Comparator;

/**
 * 短作业优先调度算法
 * 运行时间越短，排列越靠前
 */
public class SJF implements Comparator<ProcessScheduling.Process> {
    @Override
    public int compare(ProcessScheduling.Process p1, ProcessScheduling.Process p2) {
        return p1.runningTime > p2.runningTime ?
                (p1.runningTime == p2.runningTime ? 0 : 1)
                : -1;
    }
}
