/**
 * Bank simulation with three tellers and 50 customers. Demonstrate use of shared resources and
 * thread coordination via Semaphores, and using mutexes in Java.
 * Ezzah Qureshi Nov 7th, 2025
 */

import java.util.Random;
import java.util.concurrent.Semaphore;
public class Bank {
    // constants
    static int TellerCount = 3;
    static int customerCount = 50;

    // Semaphores for shared resources
    static Semaphore safe = new Semaphore(2); // only 2 tellers go into safe at a time
    static Semaphore manager = new Semaphore(1); // only 1 teller talks to manager at a time
    static Semaphore door = new Semaphore(2); // only 2 customers walk through door at a time

    // Semaphores for coordination between teller and customer
    static Semaphore[] isTellerReady = new Semaphore[TellerCount]; // signal customer that teller is ready
    static Semaphore[] customerGiveID = new Semaphore[TellerCount]; // signal teller that customer is there
    static Semaphore[] tellerAskType = new Semaphore[TellerCount]; // signal customer to give transaction type
    static Semaphore[] customerGiveType = new Semaphore[TellerCount]; // signal teller to do the transaction
    static Semaphore[] transactionDone = new Semaphore[TellerCount]; // signal customer transaction is done
    static Semaphore[] customerLeaveBank = new Semaphore[TellerCount]; // signal teller customer has left bank

    // other variables
    static final Object tellerLock = new Object(); // a lock used for handling teller array
    static final Object bankLock = new Object(); // a lock used for handling bank state
    static boolean isBankOpen = false; // boolean to see id bank is open
    static boolean[] isTellerAvailable = new boolean[TellerCount]; // boolean array that states if each teller is available
    static int tellersReadyCount = 0; // counter
    static int customersAllServed = 0; // counter
    static int[] customerIdArray = new int[TellerCount]; // stores the customer's id currently being served
    static String[] customerTransactionArray = new String[TellerCount]; // stores the transactions that the customer wanted
    static int[] customerAtTeller = new int[TellerCount]; // keep track of which customer is being served at each teller

    public static void main(String[] args) {
        // initialize semaphores
        for (int i = 0; i < TellerCount; i++) {
            isTellerReady[i] = new Semaphore(0);
            customerGiveID[i] = new Semaphore(0);
            tellerAskType[i] = new Semaphore(0);
            customerGiveType[i] = new Semaphore(0);
            transactionDone[i] = new Semaphore(0);
            customerLeaveBank[i] = new Semaphore(0);
            isTellerAvailable[i] = false;
        }

        // create and start teller threads
        Thread[] tellers = new Thread[TellerCount];
        for (int i = 0; i < TellerCount; i++) {
            //start teller thread
            tellers[i] = new Teller(i);
            tellers[i].start();
        }

        // create and start customer threads
        Thread[] customers = new Thread[customerCount];
        for (int i = 0; i < customerCount; i++) {
            //start customer thread
            customers[i] = new Customer(i);
            customers[i].start();
        }

        // when threads are done close the program/bank
        try {
            for (Thread teller : tellers) teller.join();
            for (Thread customer : customers) customer.join();
        } catch (InterruptedException e) {
                System.err.println("Error joining with Thread" + e);
            }
        System.out.println("Bank is now closed");
    }

    /**
     * Teller class handles the teller logic
     */
    static class Teller extends Thread {
        int id; // teller id
        private final Random random = new Random();

        public Teller(int id) {
            this.id = id;
        }

        @Override
        public void run() {

            // all three tellers must be ready
            System.out.println("Teller " + id + " []: ready to serve");

            // when all three tellers are ready the bank opens
            synchronized (tellerLock) {
                isTellerAvailable[id] = true;
            }

            // lock so each teller goes in and increments count
            synchronized (bankLock) {
                tellersReadyCount++;
                // when tellers ready is 3, the bank opens
                if (tellersReadyCount == TellerCount) {
                    isBankOpen = true;
                }
            }

            // each teller thread can work with multiple customers, loop until all customers are served
            while(true) {
                // check if all 50 have been served
                // if they have been, get out of loop
                synchronized (bankLock) {
                    if (customersAllServed >= customerCount) {
                        break;
                    }
                }

                // teller waits for a customer
                System.out.println("Teller " + id + " []: waiting for a customer");

                // if this is teller's second or more customer, its available boolean is probably false, switch it to true
                synchronized (tellerLock) {
                    isTellerAvailable[id] = true;
                }

                // we have to signal customer to come to teller
                // need to make sure that the same teller is talking to the same customer --> array of semaphores, index is id
                isTellerReady[id].release();

                // teller has to wait for customer to come to them and give their id
                // teller will try to get signal from semaphore, if its already released teller should be able to acquire it
                try {
                    customerGiveID[id].acquire();
                } catch (InterruptedException e) {
                    continue;
                }

                // Double-check after acquiring - in case customer was the last one
                synchronized (bankLock) {
                    if (customersAllServed >= customerCount) {
                        break;
                    }
                }

                // store customer's id, use array to store all ids
                int customerID = customerIdArray[id];

                // once given id, teller will ask for transaction type
                System.out.println("Teller " + id + " [Customer " + customerID + "]: serving a customer");
                System.out.println("Teller " + id + " [Customer " + customerID + "]: asks for transaction");

                // send signal to customer to answer transaction type, release()
                tellerAskType[id].release();

                // wait for customer to give an answer, acquire
                try {
                    customerGiveType[id].acquire();
                } catch (InterruptedException e) {
                    continue;
                }

                // store transaction that customer gives
                String transaction = customerTransactionArray[id];

                // if withdrawal go to manager
                if (transaction.equals("withdrawal")) {
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: handling a withdrawal");
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: going to manager");

                    // semaphore here, acquire? only one teller to manager
                    try {
                        // resource sharing manager
                        manager.acquire();
                        System.out.println("Teller " + id + " [Customer " + customerID + "]: getting to managers permission");

                        // random time to simulate work
                        int managerTime = 5 + random.nextInt(26);
                        Thread.sleep(managerTime);

                        System.out.println("Teller " + id + " [Customer " + customerID + "]: got to manager's permission");
                        // semaphore, release?
                        manager.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: handling a deposit");
                }

                // go to safe
                System.out.println("Teller " + id + " [Customer " + customerID + "]: going to safe");

                // semaphore for safe, only 2 tellers at a time, acquire
                try {
                    safe.acquire();
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: enter safe");

                    //random time to simulate work
                    int safeTime = 10 + random.nextInt(41);
                    Thread.sleep(safeTime);

                    // semaphore release
                    System.out.println("Teller " + id + " [Customer " + customerID + "]: leaving safe");
                    safe.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // tell and signal customer transaction is done
                System.out.println("Teller " + id + " [Customer " + customerID + "]: finishes " + transaction);
                System.out.println("Teller " + id + " [Customer " + customerID + "]: waits for customer to leave");
                transactionDone[id].release();

                // semaphore to wait until customer actually left
                try {
                    customerLeaveBank[id].acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Teller " + id + "[]: leaves bank");
        }
    }

    /**
     * customer class handles customer logic
     */
    static class Customer extends Thread {
        int id;
        private final Random random = new Random();

        public Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            // customer decides transaction type (random)
            String transaction = random.nextBoolean() ? "deposit" : "withdrawal";
            System.out.println("Customer " + id + " []: wants to perform a " + transaction);

            // wait random time
            try {
                int wait = random.nextInt(100);
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // wait for bank to open, forever loop until it is
            while(!isBankOpen) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // enter bank through the door
            System.out.println("Customer " + id + " []: goes to bank");

            // semaphore, door is shared only two customers at a time
            try {
                door.acquire();
                System.out.println("Customer " + id + " []: entering bank");
                door.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // get in line and wait for teller
            System.out.println("Customer " + id + " []: getting in line");

            // set a value that represents teller is busy
            int tellerID = -1;

            // loop until tellerIndex is an actual teller (customer found teller)
            while (tellerID == -1) {
                // customer scans all three tellers and sees if any are free
                synchronized (tellerLock) {
                    for (int i = 0; i < TellerCount; i++) {
                        // if a teller is free, take it
                        if (isTellerAvailable[i]) {
                            // set bool to false/busy and store teller id
                          isTellerAvailable[i] = false;
                          tellerID = i;
                          customerAtTeller[i] = id;
                          customerIdArray[i] = id;
                          break;
                        }
                    }
                }

                // if no teller was free
                if (tellerID == -1) {
                    try {
                        for (int i = 0; i < TellerCount; i++) {
                            // try to acquire a signal in case teller becomes free
                            if (isTellerReady[i].tryAcquire()) {
                                synchronized (tellerLock) {
                                    // if free set bool to busy and store teller id
                                    if (isTellerAvailable[i]) {
                                        isTellerAvailable[i] = false;
                                        tellerID = i;
                                        customerAtTeller[i] = id;
                                        customerIdArray[i] = id;
                                        break;
                                        // if not release the signal back
                                    } else {
                                        isTellerReady[i].release();
                                    }
                                }
                            }
                        }
                        // still no free teller sleep
                        if (tellerID == -1) {
                            Thread.sleep(10);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // customer selects a teller and gives ID
            System.out.println("Customer " + id + " []: selecting a teller");
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: selects teller");
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: introduces itself");
            // signal teller that is has approached teller
            customerGiveID[tellerID].release();

            // wait for teller to ask for transaction type acquire it
            try {
                tellerAskType[tellerID].acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // give teller transaction type and signal
            customerTransactionArray[tellerID] = transaction;
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: asks for " + transaction);
            customerGiveType[tellerID].release();

            // wait for transaction to complete
            try {
                transactionDone[tellerID].acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // leave the bank signal teller
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: leaves teller");

            // need to increment the count of customers served, but only one thread does it at a time --> lock
            synchronized (bankLock) {
                customersAllServed++;
            }

            System.out.println("Customer " + id + " []: goes to door");
            System.out.println("Customer " + id + " []: leaves the bank");

            customerLeaveBank[tellerID].release();

            // if this is the last customer make sure to let the other existing teller threads know
            // otherwise tellers will endlessly wait for a non-existing customer
            if (customersAllServed >= customerCount) {
                for (int i = 0; i < TellerCount; i++) {
                    customerGiveID[i].release();
                }
            }
        }
    }
}