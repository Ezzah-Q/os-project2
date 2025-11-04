import java.util.Random;
import java.util.concurrent.Semaphore;
public class Bank {
    int TellerCount = 3;
    int customerCount = 1;

    // Semaphores
    Semaphore safe = new Semaphore(2);
    Semaphore manager = new Semaphore(1);
    Semaphore door = new Semaphore(2);
    Semaphore bankOpen = new Semaphore(0);
    Semaphore isTellerReady = new Semaphore(TellerCount);
    Semaphore customerGiveID = new Semaphore(TellerCount);
    Semaphore tellerAskType = new Semaphore(TellerCount);
    Semaphore customerGiveType = new Semaphore(TellerCount);
    Semaphore transactionDone = new Semaphore(TellerCount);
    Semaphore customerLeaveBank = new Semaphore(TellerCount);

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
        // teller id
        int id;

        // constructor
        public Teller(int id) {
            this.id = id;
        }

        @Override
        public void run() {

            // all three tellers must be ready
            System.out.println("Teller " + id + " []: ready to serve");

            // when all three tellers are ready the bank opens
            // some sort of logic to see when all three tellers are available, which then opens the bank
            // maybe bankOpen should be some boolean? which is set to true when all three are ready

            // teller waits for a customer
            System.out.println("Teller " + id + " []: waiting for a customer");

            // customer comes up, store their id
            int customerID;
            System.out.println("Teller " + id + " [Customer " + customerID + "]: serving a customer");

            // teller asks for transaction type
            System.out.println("Teller " + id + " [Customer " + customerID + "]: asks for transaction");

            // teller waits for transaction

            // store transaction
            String transaction;

            // if withdrawal go to manager
            if (transaction.equals("withdrawal")) {
                System.out.println("Teller " + id + " [Customer " + customerID + "]: handling a withdrawal");
                System.out.println("Teller " + id + " [Customer " + customerID + "]: going to manager");

                // logic to get manager's permission
            } else {
                System.out.println("Teller " + id + " [Customer " + customerID + "]: handling a deposit");
            }

            // go to safe
            System.out.println("Teller " + id + " [Customer " + customerID + "]: going to safe");
            // logic for safe

            // tell customer transaction is done
            System.out.println("Teller " + id + " [Customer " + customerID + "]: finishes " + transaction);

            // wait for customer to leave
            System.out.println("Teller " + id + " [Customer " + customerID + "]: waits for customer to leave");
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
