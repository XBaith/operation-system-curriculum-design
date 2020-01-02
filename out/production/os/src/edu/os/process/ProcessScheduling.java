package edu.os.process;

import edu.os.process.dispatcher.FCFS;
import edu.os.process.dispatcher.Priority;
import edu.os.process.dispatcher.SJF;
import java.util.*;

public class ProcessScheduling {

    /**
     * 进程
     */
    public static class Process {
        /*PCB信息*/
        public String name;    //进程名
        public int arrivalTime;    //到达时间
        public int runningTime;    //运行时间
        public int priority;   //优先级
        /*进程信息*/
        public int inMemoTime;  //进入内存时间
        public int finishTime;  //完成时间
        public boolean running;    //进程状态是否在运行
        public int clock;   //服务时间

        Process(String name, int arrivalTime, int runningTime, int priority){
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.runningTime = runningTime;
            this.priority = priority;
            inMemoTime = -1;
            finishTime = 0;
            running = false;
            clock = runningTime;
        }

        Process(){
            this("-", -1, -1, -1);
        }

        /**
         * 获得周转时间
         * @return  周转时间
         */
        public int getCycleTime() {
            if(finishTime == 0)
                return 0;
            return finishTime - arrivalTime;
        }

        /**
         * 获得带权周转时间
         * @return  带权周转时间(字符串)
         */
        public String getWeightCycleTime() {
            if(getCycleTime() == 0)
                throw new IllegalArgumentException("Server time is zero, please check!");
            return   getCycleTime() +  "/" + clock;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("\t")
            .append(arrivalTime).append("\t")
            .append(runningTime).append("\t")
            .append(priority);
            return sb.toString();
        }
    }

    private static Comparator<Process> jobDispatcher; //作业调度算法
    private static Comparator<Process> processDispatcher;  //进程调度算法
    private static Queue<Process> jobs;   //作业队列
    private static Queue<Process> readyQueue;  //就绪队列，按照作业调度算法排序
    private static ArrayList<Process> memoQueue ;   //内存中的进程作业，按照进程调度算法排序
    private static Queue<Process> doneQueue = new LinkedList<>();  //完成队列
    private static int procNum; //进程总数
    private final static int MULTICHANNEL = 1;  //操作系统道数
    private static boolean isRR = false;    //是否是时间片轮转算法

    /**
     * 输入调度算法和进程PCB信息
     */
    private static void selectScheduling() {
        Scanner in = new Scanner(System.in);
        System.out.println("Select job dispatcher");
        System.out.println("1.First-Come First-Served\t2.Shortest Job First\t3.No job dispatcher");
        int jd = in.nextInt();
        switch (jd) {
            case 1:
                jobs = new PriorityQueue<>(new FCFS());
                jobDispatcher =  new FCFS();
                readyQueue = new PriorityQueue<>(jobDispatcher);
                break;
            case 2:
                jobs = new PriorityQueue<>(new FCFS());
                jobDispatcher = new SJF();
                readyQueue = new PriorityQueue<>(jobDispatcher);
                break;
            default:
                jobs = new LinkedList<>();
                readyQueue = new LinkedList<>();
                break;
        }
        System.out.println("Select process dispatcher");
        System.out.println("1.Round Robin\t2.Priority\t3.No process dispatcher");
        int pd = in.nextInt();
        switch (pd) {
            case 1:
                memoQueue = new ArrayList<>(MULTICHANNEL);
                isRR = true;
                break;
            case 2:
                processDispatcher = new Priority();
                memoQueue = new ArrayList<>(MULTICHANNEL);
                if(jd == 3)
                    readyQueue = new PriorityQueue<>(processDispatcher);
                break;
            default:
                processDispatcher = new FCFS();
                memoQueue = new ArrayList<>(MULTICHANNEL);
                break;
        }
        //输入进程总数
        System.out.println("Enter process number");
        procNum = in.nextInt();

        //输入进程名
        System.out.println("Name processes");
        String[] names = new String[procNum];
        for(int i = 0; i < procNum; i++)
            names[i] = in.next();

        //输入进程到达时间
        System.out.println("Enter processes arrival time");
        Integer[] arrTimes = new Integer[procNum];
        for(int i = 0; i < procNum; i++)
            arrTimes[i] = in.nextInt();

        //输入进程估计运行时间
        System.out.println("Enter processes running time");
        Integer[] runTimes = new Integer[procNum];
        for(int i = 0; i < procNum; i++)
            runTimes[i] = in.nextInt();

        Integer[] priorities = new Integer[procNum];
        if(pd == 2){
            System.out.println("Enter processes priority");
            for(int i = 0; i < procNum; i++)
                priorities[i] = in.nextInt();
        } else {
            for(int i = 0; i < procNum; i++)
                priorities[i] = -1;
        }

        //将进程加入到进程队列
        for(int p = 0; p < procNum; p++)
            jobs.add(new Process(names[p], arrTimes[p], runTimes[p], priorities[p]));
    }

    /**
     * 计算出合适的时间片长度，过大过小都不合适，比平均的时间片稍长一点比较合适
     * @return 时间片长度
     */
    private static int getTimeSplit() {
        if(jobs.isEmpty())
            return 0;
        int sum = 0;
        for(Process p : jobs)
            sum += p.runningTime;

        return sum / jobs.size() + 1;
    }

    /**
     * 时间片轮转的进程调度算法
     * @param clock 全局时间
     */
    private static int RR(int clock, int split) {
        int left = 0;
        if(onlyOneRRProcess()) {
            lastProcess = readyQueue.poll();
            lastProcess.runningTime -= split;
            if (lastProcess.runningTime <= 0) {   //如果运行进程完成
                lastProcess.finishTime = clock + split + lastProcess.runningTime;
                //如果没有在单位时间片内执行完需要把剩余的时间返回，即时间倒流
                left = lastProcess.runningTime;
                System.out.println("Process : " + lastProcess.name + " DONE at " + lastProcess.finishTime);
                doneQueue.add(lastProcess);
                lastProcess = null;
                return left;
            }

            readyQueue.add(lastProcess);
            lastProcess = null;
            return left;
        }

        while(!jobs.isEmpty()){
            if(clock == jobs.peek().arrivalTime){
                if(readyQueue.isEmpty() && runProcess == null) {
                    Process p = jobs.poll();
                    if (p.inMemoTime == -1) {
                        p.inMemoTime = clock;
                        System.out.println("Process : " + p.name + " ENTER at " + clock);
                    }
                    runProcess = p;
                    runProcess.running = true;
                }else {
                    readyQueue.add(jobs.poll());
                }
            }else break;
        }
        //每次运行完单位时间片之后，需要从就绪队列中取出运行该进程
        if(!readyQueue.isEmpty() && runProcess == null) {
            Process p = readyQueue.poll();
            if(p.inMemoTime == -1) {
                p.inMemoTime = clock;
                System.out.println("Process : " + p.name + " ENTER at " + clock);
            }
            runProcess = p;
        }
        //最后将上次运行的进程添加到就绪队列的队尾
        if(lastProcess != null) {
            readyQueue.add(lastProcess);
            lastProcess = null;
        }

        //轮转时间片
        if (runProcess != null ) {
            runProcess.runningTime -= split;
            if(runProcess.runningTime <= 0) {   //如果运行进程完成
                runProcess.finishTime = clock + split + runProcess.runningTime;
                //如果没有在单位时间片内执行完需要把剩余的时间返回，即时间倒流
                left = runProcess.runningTime;
                System.out.println("Process : " + runProcess.name + " DONE at " + runProcess.finishTime);
                doneQueue.add(runProcess);
                runProcess = null;
                lastProcess = null;
            } else {    //运行进程还未完成
                lastProcess = runProcess;
                runProcess = null;
            }
        }

        return left;
    }

    private static void notRR(int clock){
        if (onlyOneProcess()) {
            runProcess = memoQueue.get(0);
            runProcess.running = true;
        }

        while (!jobs.isEmpty() || !readyQueue.isEmpty()) {
            if (!jobs.isEmpty() && clock == jobs.peek().arrivalTime) {  //作业队列中还有进程
                if (memoQueue.size() < MULTICHANNEL && readyQueue.isEmpty()) {
                    //如果内存中的进程总数没到操作系统道数且就绪队列为空，就直接放入内存，根据进程调度算法决定谁先分配CPU
                    Process p = jobs.poll();
                    p.inMemoTime = clock;
                    memoQueue.add(p);
                    System.out.println("Process : " + p.name + " ENTER at " + clock);
                    runProcess = memoQueue.get(0);
                    runProcess.running = true;
                } else  //否则先加入就绪队列，之后根据作业调度算法决定谁先进入内存
                    readyQueue.add(jobs.poll());
            } else if (runProcess != null) break; //有正在运行的进程

            if (!readyQueue.isEmpty()) { //作业队列为空，就绪队列不为空
                if (memoQueue.size() < MULTICHANNEL) {
                    Process p = readyQueue.poll();
                    p.inMemoTime = clock;
                    memoQueue.add(p);
                    System.out.println("Process : " + p.name + " ENTER at " + clock);
                    runProcess = memoQueue.get(0);
                    runProcess.running = true;
                } else break;
            }
        }

        //花费运行时间
        if (runProcess != null && --runProcess.runningTime == 0) {
            runProcess.finishTime = clock + 1;
            System.out.println("Process : " + runProcess.name + " DONE at " + runProcess.finishTime);
            memoQueue.remove(runProcess);
            doneQueue.add(runProcess);
            runProcess = null;
        }
    }

    private static String showResult() {
        StringBuilder res = new StringBuilder();
        int avgCycleTime = 0, avgWeightCycleTime = 0;
        res.append("Process-Name\tArrival-Time\tRunning-Time\tPriorty\tEnter-Time\tFinish-Time\tCycle-Time\tWeight-Cycle-Time\n");
        for(Process p : doneQueue){
            res.append(p.name + "\t\t\t\t").append(p.arrivalTime + "\t\t\t\t")
                    .append(p.clock + "\t\t\t\t").append(p.priority + "\t\t")
                    .append(p.inMemoTime + "\t\t\t").append(p.finishTime + "\t\t\t")
                    .append(p.getCycleTime() + "\t\t\t").append(p.getWeightCycleTime() + "\n");
            avgCycleTime += p.getCycleTime();
        }
        res.append("Average cycle time : " + avgCycleTime);

        return res.toString();
    }

    /**
     * 系统中就剩一个进程还没有运行，并且在内存中
     * @return  是否符合条件
     */
    private static boolean onlyOneProcess() {
        return memoQueue.size() == 1 && readyQueue.isEmpty() && jobs.isEmpty() && runProcess == null;
    }
    private static boolean onlyOneRRProcess() {
        return readyQueue.size() == 1 && jobs.isEmpty() && runProcess == null && lastProcess == null;
    }

    /**
     * 检测系统中所有的进程都运行完毕
     * @return 是否完成
     */
    private static boolean allDone() {
        return memoQueue.isEmpty() && readyQueue.isEmpty() && jobs.isEmpty() && runProcess == null && lastProcess == null;
    }

    /**
     * 获得所有进程的运行时间和
     * @return  运行时间和
     */
    private static int getSumTime() {
        if(jobs.isEmpty())
            return 0;
        int sum = 0;
        for(Process p : jobs)
            sum += p.runningTime;

        return sum;
    }

    /**
     * 判断是否全部进程在0时刻同时到达
     * @return  是否0时刻同时达到
     */
    private static boolean allArrivalAtZero() {
        for(Process p : jobs){
            if(p.arrivalTime != 0)
                return false;
        }
        return true;
    }

    private static Process runProcess = null;   //运行的进程
    private static Process lastProcess = null;  //时间片轮转中，上一单位时间片运行完的进程

    public static void main(String[] args) {
        selectScheduling();
        //算出系统给出的时间片
        final int split = 2;    //getTimeSplit()
        //如果都是同时到达而且进程调度是优先级调度，就全部进程直接放到就绪队列里
        if(allArrivalAtZero()) {
            for(int p = 0; p < procNum; p++)
                readyQueue.add(jobs.poll());
        }

        for(int clock = 0; !allDone(); ) {
            if (!isRR) notRR(clock++);
            else clock += RR(clock, split) + split; //进程调度算法是时间片轮转
        }

        System.out.println(showResult());
    }
}
