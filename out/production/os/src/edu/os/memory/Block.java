package edu.os.memory;

public class Block {
    protected int address;    //内存块起始地址
    protected int size;   //内存块大小
    protected boolean isFree; //内存块属性.true代表是空闲的内存块,false表示是进程分配过的

    public Block(int address, int size, boolean isFree) {
        this.address = address;
        this.size = size;
        this.isFree = isFree;
    }

    public Block(boolean isFree) {
        this(0,0,isFree);
    }

    public Block() {
        this(0,0,false);
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }
}
