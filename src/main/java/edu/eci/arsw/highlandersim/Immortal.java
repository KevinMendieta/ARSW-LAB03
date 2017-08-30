package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private int health, defaultDamageValue;
    private final List<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());
    boolean pause, running;
    private Object locker;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue) {
        super(name);
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.running = true;
        this.pause = false;
    }

    @Override
    public void run() {
        while (running) {
            if(!pause){
                tick();
            }else{
                lock();
            }
        }
    }

    /**
     * Clear all the references of the others immortals.
     */
    public void clear(){
        if(!running){
            for(Immortal im: immortalsPopulation){
                im = null;
            }
            immortalsPopulation.clear();
        }
    }

    /**
     * Set the state of the thread, false for stop the thread execution.
     * @param running the state for running.
     */
    public void setRunning(boolean running){ this.running = running;}

    /**
     * Set a locker for pausing or notify the thread in future.
     * @param locker the locker for the thread.
     */
    public void setLocker(Object locker){this.locker = locker;}

    /**
     * Set the state of the thread, true for pause the thread execution.
     * @param pause the state for pause.
     */
    public void setPause(boolean pause){this.pause = pause;}

    /**
     * Put the thread to wait when the sate of pause is true.
     */
    public void lock(){
        synchronized (locker) {
            try {
                locker.wait();
            } catch (InterruptedException e) {
                Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    /**
     * Wake up the thread when the state of pause is true.
     */
    public void unlock(){
        synchronized(locker){
            locker.notify();
        }
        pause = false;
    }

    /**
     * Make the calculation for each step in execution of the thread.
     */
    private void tick(){
        Immortal im;
        int myIndex = immortalsPopulation.indexOf(this);
        int nextFighterIndex = r.nextInt(immortalsPopulation.size());
        //avoid self-fight
        if (nextFighterIndex == myIndex) {
            nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
        }
        im = immortalsPopulation.get(nextFighterIndex);
        //synchronize the the fight avoiding deadlocks
        int myHash = this.hashCode();
        int otherHash = im.hashCode();
        if (myHash > otherHash) {
            synchronized (this) {
                synchronized (im) {
                    this.fight(im);
                }
            }
        }else if (myHash < otherHash) {
            synchronized (im) {
                synchronized (this) {
                    this.fight(im);
                }
            }
        }else{
            synchronized(locker){
                synchronized(this){
                    synchronized(im){
                        this.fight(im);
                    }
                } 
            }
        }
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void fight(Immortal i2) {

        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            System.out.println("Fight: " + this + " vs " + i2);
        } else {
            System.out.println(this + " says:" + i2 + " is already dead!");
        }

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }

}
