import java.util.Random;
import java.util.concurrent.Semaphore;
public class Bank {
    static int TellerCount = 3;
    static int customerCount = 1;

    // Semaphores for shared resources
    static Semaphore safe = new Semaphore(2);
    static Semaphore manager = new Semaphore(1);
    static Semaphore door = new Semaphore(2);

    // Semaphores for coordination between teller and customer
    static Semaphore[] isTellerReady = new Semaphore[TellerCount]; //when teller is busy customer must wait
    static Semaphore[] customerGiveID = new Semaphore[TellerCount];
    static Semaphore[] tellerAskType = new Semaphore[TellerCount];
    static Semaphore[] customerGiveType = new Semaphore[TellerCount];
    static Semaphore[] transactionDone = new Semaphore[TellerCount];
    static Semaphore[] customerLeaveBank = new Semaphore[TellerCount];

    // other variables
    static final Object tellerLock = new Object();
    static final Object bankLock = new Object();
    static boolean isBankOpen = false;
    static boolean[] isTellerAvailable = new boolean[TellerCount];
    static int tellersReadyCount = 0;
    static int customersAllServed = 0;
    static int[] customerIdArray = new int[TellerCount];
    static String[] customerTransactionArray = new String[TellerCount];

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
        private final Random random = new Random();

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
                // use semaphore.release to signal that teller is ready
                // need to make sure that the same teller is talking to the same customer --> array of semaphores, index is id
                isTellerReady[id].release();

                // teller has to wait for customer to come to them and give their id
                // teller will try to get signal from semaphore, if its already released teller should be able to acquire it
                try {
                    customerGiveID[id].acquire();
                } catch (InterruptedException e) {
                    continue;
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
                        throw new RuntimeException(e);
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
                    throw new RuntimeException(e);
                }

                // tell and signal customer transaction is done
                System.out.println("Teller " + id + " [Customer " + customerID + "]: finishes " + transaction);
                transactionDone[id].release();

                // wait for customer to leave
                System.out.println("Teller " + id + " [Customer " + customerID + "]: waits for customer to leave");
                // semaphore to wait until customer actually left
                try {
                    customerLeaveBank[id].acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("Teller " + id + "[]: leaves bank");
        }
    }

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
            int wait = random.nextInt(100);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // wait for bank to open, forever loop until it is
            while(!isBankOpen) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }

            // get in line and wait for teller
            System.out.println("Customer " + id + " []: getting in line");

            // either teller is available

            // teller is not available --> wait for teller to be available


            // customer selects a teller and gives ID
            System.out.println("Customer " + id + " []: selecting a teller");
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: selects teller");
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: introduces itself");
            // signal teller that is has approached teller
            customerGiveID[tellerID].release();

            // wait for teller to ask for transaction type acquire it

            tellerAskType[tellerId].acquire();

            // give teller transaction type and signal
            customerTransactionArray[tellerID] = transaction;
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: asks for" + transaction);
            customerGiveType[tellerID].release();

            // wait for transaction to complete
            transactionDone[tellerID].acquire();

            // leave the bank signal teller
            System.out.println("Customer " + id + " [Teller " + tellerID + "]: leaves teller");
            customerLeaveBank[tellerID].release();

            System.out.println("Customer " + id + " []: goes to door");
            System.out.println("Customer " + id + " []: leaves the bank");

            // need to increment the count of customers served, but only one thread does it at a time --> lock
            synchronized (bankLock) {
                customerCount++;
            }
        }
    }
}