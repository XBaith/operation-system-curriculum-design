package edu.os.memory;

import java.util.*;

public class DynamicMemoryAllocation {

    private LinkedList<Block> freeBlocks;   //空闲内存块链
    private LinkedList<Block> processBlocks; //进程块链
    private int memoSize;   //内存大小
    private int freeSize;    //剩余空闲区大小
    private int minSize;    //不可分割的最小空闲区大小
    private Comparator<Block> comparator;  //空闲分区的排序方法
    /*内存块分配算法的枚举*/
    enum ALG {
        FIRST_FIT, NEXT_FIT, BEST_FIT
    }
    private ALG alg;    //动态分区算法

    public DynamicMemoryAllocation(int memoSize, int minSize) {
        freeBlocks = new LinkedList<>();
        processBlocks = new LinkedList<>();
        this.memoSize = memoSize;
        this.freeSize = memoSize;
        this.minSize = minSize;
        Block freeBlock =  new Block(0, freeSize, true);
        freeBlocks.add(freeBlock);
    }

    private int lastIndex = 0;  //上次遍历到的空闲内存块索引

    /**
     * 计算经过分配和回收之后，剩余的内存块分布情况
     * @return 剩余内存卡分布情况
     */
    public String calculiFreeBlock() {
        Scanner in = new Scanner(System.in);
        System.out.println("Choice a dynamic allocation algorithm\n1.first fit\t2.next fit\t3.best fit");
        int algNum = in.nextInt();
        alg = algNum == 1 ? ALG.FIRST_FIT : (algNum == 2 ? ALG.NEXT_FIT : ALG.BEST_FIT);
        System.out.println("Positive number means allocating memory, Negative number means free memory");
        System.out.println("Allocate/Free memory size:");
        while(in.hasNext() && freeSize > minSize) {
            System.out.println("Allocate/Free memory size:");
            int opSize = in.nextInt();   //正数表示申请内存，负数表示释放内存
            if(opSize > 0) {
                allocMemo(opSize);
            } else if(opSize < 0){
                freeMemo(-opSize);
            } else break;
        }
        
        return printResult();
    }

    /**
     * 申请内存块
     * @param allocSize 申请内存块大小
     */
    private void allocMemo(int allocSize) {
        if(freeSize < allocSize) 
            throw new IllegalArgumentException("Allocation is illegal!");

        switch (alg) {
            case FIRST_FIT: {
                comparator = (Block a, Block b) -> a.address > b.address ? 1 : (a.address == b.address ? 0 : -1);
                for(Block block : freeBlocks) {
                    if(block.size >= allocSize) {
                        Block process = new Block(block.address, allocSize, false);
                        processBlocks.add(process);
                        //更新内存块起始地址和大小
                        block.setAddress(block.address + allocSize);
                        block.setSize(block.size - allocSize);
                        freeSize -= allocSize;
                        if(block.size <= minSize) {    //如果剩余内存块太小了就直接释放，避免生成不可用的内存块
                            freeMemo(block.size);
                        }
                        break;
                    }
                }
                break;
            }
            case NEXT_FIT: {
                comparator = (Block a, Block b) -> a.address > b.address ? 1 : (a.address == b.address ? 0 : -1);
                for(int i = lastIndex; i < freeBlocks.size(); i++) {
                    Block block = freeBlocks.get(i);
                    if(block.size >= allocSize) {
                        Block process = new Block(block.address, allocSize, false);
                        processBlocks.add(process);
                        //更新内存块信息
                        block.setAddress(block.address + allocSize);
                        block.setSize(block.size - allocSize);
                        freeSize -= allocSize;
                        if(block.size <= minSize) {    //如果剩余内存块太小了就直接释放，避免生成不可用的内存块
                            freeMemo(block.size);
                        }
                        break;
                    }
                }
                break;
            }
            case BEST_FIT: {
                comparator = (Block a, Block b) -> a.size > b.size ? 1 : (a.size == b.size ? 0 : -1);
                for(Block block : freeBlocks) {
                    if(block.size >= allocSize) {
                        Block process = new Block(block.address, allocSize, false);
                        processBlocks.add(process);
                        block.setAddress(block.address + allocSize);
                        block.setSize(block.size - allocSize);
                        freeSize -= allocSize;
                        if(block.size <= minSize) {    //如果剩余内存块太小了就直接释放，避免生成不可用的内存块
                            freeMemo(block.size);
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * 回收内存
     * @param freeSize  回收内存大小
     */
    private void freeMemo(int freeSize) {
        if (memoSize < freeSize)
            throw new IllegalArgumentException("Free operation is illegal!");

        boolean flag = false;   //检测释放的内存是否之前申请过
        for (Block process : processBlocks) {
            if (process.size == freeSize) {
                //检测上下内存块情况并更新链表
                checkBlock(process);
                this.freeSize += process.size;
                flag = true;
                break;
            }
        }
        //没有找到可以回收的内存
        if (!flag)
            System.err.println("The memory hasn't allocate before!");
    }

    /**
     * 更新空闲内存块链表
     * @param process   待释放的进程
     */
    private void checkBlock(Block process) {
        processBlocks.remove(process);  //从进程链表中删除
        for(int i = 0; i < freeBlocks.size() - 1; i++) {
            Block pre = freeBlocks.get(i);
            Block next = freeBlocks.get(i + 1);
            if(pre.address + pre.size == process.address) { //上临
                if(next.address == process.address + process.size) {    //上临下临
                    freeBlocks.remove(next);
                    pre.setSize(pre.size + process.size + next.size);
                } else {    //上临下不临
                    pre.setSize(pre.size + process.size);
                }
            } else {    //上不临
                if(next.address == process.address + process.size) {    //上不临下临
                    next.setAddress(process.address);
                    next.setSize(process.size + next.size);
                } else {    //上下都不临
                    freeBlocks.add(i + 1, process);break;
                }
            }
        }
        if(freeBlocks.size() <= 1)
            freeBlocks.add(process);
        //更新完之后，需要按照排序规则将空闲区内存排序
        Collections.sort(freeBlocks, comparator);
    }

    /**
     * 拼接最终结果字符串
     * @return  结果字符串
     */
    private String printResult() {
        StringBuilder builder = new StringBuilder();
        builder.append("Size\tAddress\n");
        for(Block block : freeBlocks)
            builder.append(block.size + "\t\t" + block.address + "\n");
        return builder.toString();
    }

    public static void main(String[] args) {
        DynamicMemoryAllocation allocation = new DynamicMemoryAllocation(512, 2);
        System.out.println(allocation.calculiFreeBlock());
    }

}
