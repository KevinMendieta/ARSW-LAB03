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
    boolean pause = false;
    private Object locker;

    public void pause() {
        pause = true;
    }

    public void cont() {
        pause = false;
    }

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue) {
        super(name);
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.locker = new Object();
    }

    @Override
    public void run() {
        while (true) {
            if(!pause){
                tick();
            }else{
                lock();
            }
        }
    }
    
    /**
     * 
     * @param pause 
     */
    public void setPause(boolean pause){this.pause = pause;}

    /**
     * 
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
     * 
     */
    public void unlock(){
        synchronized(locker){
            locker.notify();
        }
        pause = false;
    }

    /**
     * 
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
        this.fight(im);
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
