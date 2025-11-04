11-02-25 11:21 pm
- this is the first entry
- here is what I know about the project: 
  - bank simulation with 3 tellers and 50 customers
  - bank opens doors when all 3 tellers are ready
  - customers will visit the bank, but first they have to choose their transaction, wait, and then enter
    - and they can only enter through doors 2 at a time --> probably a need for a semaphore
  - customer will get in line
  - available teller will tell line that it is ready to serve --> semaphore, customer thread waits until teller free
  - customer goes to the teller and give its id to the teller --> semaphore, teller wait for id
  - teller will ask for transaction --> semaphore, customer waits
  - customer will tell teller the transaction (withdraw or deposit) --> semaphore, teller waits
    - if transaction is withdrawing, teller will go to manager --> only 1 at a time, use semaphore
      - wait random time from 5 to 30 ms
    - teller will go to safe --> only 2 allowed so use semaphore
      - at safe block time 10-50 ms
  - when teller is done with transaction it will return to customer and say transaction is done
  - customer will the leave the bank/exit program --> semaphore, customer should wait until transaction is done
  - once customer leaves teller becomes available --> semaphore, teller should wait until customer leaves bank

- shared resources: door, manager, safe
- additional semaphores needed:
  1. in the beginning, customers must wait if bank is closed
  2. when teller is busy customer must wait
  3. teller must wait for customer until they've given their id
  4. customer waits for teller to ask transaction type
  5. teller waits for customer to give transaction type
  6. customer must wait until teller is done with transaction
  7. teller waits for customer to leave bank

- for this session I will write the semaphores I brainstormed just now

11-03-25 6:57 pm 
- this is before session
- I forgot to mention that I will write this code in IntelliJ using Java
- this will be my first session, in the last entry I implied that that was my first session, but I am doing it today instead
- for this session I plan on writing the semaphore I planned out and maybe writing additional pseudocode for the program