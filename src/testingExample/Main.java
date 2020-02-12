public class Main {
    static int n = 10;

    static void countdown(){
        int loop;
        for(loop = 0; loop < 5; loop++){
            n--;
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(Main::countdown);
        Thread t2 = new Thread(Main::countdown);
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("The value of n is " + n);
    }

}
