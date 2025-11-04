import java.util.Random;
import java.util.concurrent.Semaphore;
public class Bank {
    static int TellerCount = 3;
   static int customerCount = 1;

    // Semaphores
    static Semaphore safe = new Semaphore(2);
    static Semaphore manager = new Semaphore(1);
    static Semaphore door = new Semaphore(2);
    //static Semaphore bankOpen = new Semaphore(0);
    static Semaphore isTellerReady = new Semaphore(TellerCount);
    static Semaphore customerGiveID = new Semaphore(TellerCount);
    static Semaphore tellerAskType = new Semaphore(TellerCount);
    static Semaphore customerGiveType = new Semaphore(TellerCount);
    static Semaphore transactionDone = new Semaphore(TellerCount);
    static Semaphore customerLeaveBank = new Semaphore(TellerCount);
    static final Object tellerLock = new Object();
    static final Object bankLock = new Object();
    static boolean isBankOpen = false;
    static boolean[] isTellerAvailable = new boolean[TellerCount];
    static int tellersReadyCount = 0;
    static int customersAllServed = 0;

    public void main(String[] args) {
        // create and start teller threads
        Thread[] tellers = new Thread[TellerCount];
        for (int i = 0; i < TellerCount; i++) {
            //start teller thread
            tellers[i] = new Thread(new Teller(i));
            tellers[i].start();
        }

        // create and start customer threads
        Thread[] customers = new Thread[customerCount];
        for (int i = 0; i < customerCount; i++) {
            //start customer thread
            customers[i] = new Thread(new Customer(i));
            customers[i].start();
        }

        // when threads are done close the program/bank
        try {
            for (Thread teller : tellers) {
                teller.join();
            }
            for (Thread customer : customers) {
                customer.join();
            }
        } catch (InterruptedException e) {
                System.err.println("Error joining with Thread" + e);
            }
        System.out.println("Bank is now closed");

    }

    static class Teller extends Thread {
        int id; // teller id

        public Teller(int id) {
            this.id = id;
        } // constructor

        @Override
        public void run() {

            // all three tellers must be ready
            System.out.println("Teller " + id + " []: ready to serve");

            // when all three tellers are ready the bank opens
            // some sort of logic to see when all three tellers are available, which then opens the bank
            // maybe bankOpen should be some boolean? which is set to true when all three are ready
            // lock so that each teller says they are ready
            synchronized (tellerLock) {
                isTellerAvailable[id] = true;
            }

            // lock so each teller goes in and increments count
            synchronized (bankLock) {
                tellersReadyCount++;
                if (tellersReadyCount == TellerCount) {
                    isBankOpen = true;
                }
            }

            // each teller thread can work with multiple customers, loop until all customers are served
            boolean b = true;
            while(b) {
                // check if all 50 have been served
                // if they have been false --> get out of loop
                // if not then continue
                // this needs to be in lock since only one thread needs to do this
                synchronized (bankLock) {
                    if (customersAllServed == customerCount) {
                        b = false;
                    }
                }

                // teller waits for a customer
                System.out.println("Teller " + id + " []: waiting for a customer");

                // if this is teller's second or more customer, its available boolean is probably false, switch it to true
                synchronized (tellerLock) {
                    isTellerAvailable[id] = true;
                }

                // we have to signal customer to come to teller, semaphore?

                // customer comes up, store their id
                int customerID;
                System.out.println("Teller " + id + " [Customer " + customerID + "]: serving a customer");

                // teller asks for transaction type
                System.out.println("Teller " + id + " [Customer " + customerID + "]: asks for transaction");

                // teller waits for transaction, semaphore?

                // store transaction
                String transaction;

                // if withdrawal go to manager
                if (transaction.equals("withdrawal")) {
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: handling a withdrawal");
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: going to manager");

                    // semaphore here, aquire? only one teller to manager
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: getting to managers permission");

                    // random time

                    System.out.println("Teller " + id + " [Customer " + customerID + "]: got to manager's permission");
                    // semaphore, release?

                } else {
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: handling a deposit");
                }

                // go to safe
                System.out.println("Teller " + id + " [Customer " + customerID + "]: going to safe");
                // semaphore for safe, only 2 teller's at a time, aquire
                // semaphore release
                System.out.println("Teller " + id + " [Customer " + customerID + "]: leaving safe");

                // tell customer transaction is done
                System.out.println("Teller " + id + " [Customer " + customerID + "]: finishes " + transaction);
                // semaphore release, so customer can leave

                // wait for customer to leave
                System.out.println("Teller " + id + " [Customer " + customerID + "]: waits for customer to leave");
                // semaphore to wait until customer actually left
            }

            System.out.println("Teller " + id + "[]: leaves bank");
        }
    }

    static class Customer extends Thread {
        int id;

        public Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {

            // customer decides transaction type (random)
            String transaction;
            System.out.println("Customer " + id + " []: wants to perform a " + transaction);

            // wait random time

            // wait for bank to open

            // enter bank through the door
            System.out.println("Customer " + id + " []: goes to bank");

            // get in line and wait for teller
            System.out.println("Customer " + id + " []: getting in line");

            // give id to teller
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: gives ID");

            // wait for teller to ask for transaction type

            // give teller transaction type
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: asks for" + transaction);

            // wait for transaction to complete

            // leave the bank
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: leaves teller");


        }
    }



}
