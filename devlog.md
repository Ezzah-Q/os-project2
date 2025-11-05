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

11-03-25 10:03 pm
- after session
- I need to refresh myself on how to implement mutexes and threads in java
- I was able to write out the semaphores I was planning on using
- I wrote additional pseudocode and even mapped out the teller and customer synchronization
  - I wrote the print-out statements I needed, based off of prof's sample txt on elearning
- Next session I need to see how to signal that the three tellers are available and the bank opens
  - I originally said for there to be a bankOpen semaphore, but it could also just be a simple boolean which I set to true

11-04-25 10:19 am
- this is before session
- I did some research as to when to use mutexes vs semaphores in java
  - mutexes are for locking and unlocking and only use 1 thread at a time
    - key words: synchronized(lock) {sensitive data} 
      - lock can be any Object
      - synchronized treats the {data} as critical
  - semaphores are for limiting number of threads to a resource or signaling between threads
- I feel like instead of focusing mainly on using semaphores I can also use mutexes
- I will see if I can use mutex in figuring out if teller is available or not
- for this session I plan on figuring out teller logic and completing pseudocode for it

11-04-25 11:09 am
- after session
- this session i added locks/mutexes
  - I added two locks, one for tellers and one for the bank state itself
  - to track teller availability I used a static array (so customers can use too)
    - tellerLock allows one teller at a time manipulate this array
  - to track bank opening, I used bankLock
    - after each teller is available, one at a time they go to bankLock mutex, increment count, and 
    - as long as count = 3 then bank opens
- I made a loop in teller logic that keeps running as long as there are customers left to be serviced
  - if there isn't anymore the loop stops
- I did additional pseudocode as well for teller logic
- for next session I need to figure out semaphore logic because I am still iffy on it
- next session I want to finish coding for teller logic

11-05-25 5:35 pm
- before session
- I learned more about how to use semaphores in code
  - for resource protection, we use semaphores in this way:
    - semaphore.acquire() to get permission then
    - semaphore.release() to release permission
  - for coordination, we use semaphores in this way:
    - semaphore.release() to signal readiness, increments from 0 to 1 and then
    - semaphore.acquire() to signal that they're busy, decrements 1 to 0
  - this session I plan to add the rest of the semaphores and hopefully finish teller class