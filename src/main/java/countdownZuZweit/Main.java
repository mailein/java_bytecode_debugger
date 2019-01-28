/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;


public class Main { 
    public static Object wildcard = new Object();
    public static PseuCoThread thread = new PseuCoThread("mainAgentThread");
    public static int n = 0;
    
    public static void PseuCo_zaehler(){
        int loop = 0;
        
        for (loop = 0; loop < 5; loop++){
            
            n = n - 1;
            System.out.println("n: " + n + ", loop: " + loop);
        }
    }
    
    public static void main(String[] args) {
        
        n = 10;
        PseuCoThread a1 = new PseuCoThread("mainAgent", new Runnable() { 
            @Override
            public void run() { 
                PseuCo_zaehler();
            } 
        });
        a1.start();
        PseuCoThread a2 = new PseuCoThread("mainAgent", new Runnable() { 
            @Override
            public void run() { 
                PseuCo_zaehler();
            } 
        });
        a2.start();
        
        try {a1.join();
        } catch (InterruptedException e) {
        };
        
        try {a2.join();
        } catch (InterruptedException e) {
        };
        
        System.out.println("Der Wert ist" + n);
    }
    
}