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

11-05-25 10:24 pm
- after session
- did a lot, got a hang of semaphores used in coordination now
- finished teller code and most of the customer code
  - need to map out the customer waiting in line and find available teller logic
  - this is the plan for next session, I would like to be done with this code by then

11-06-25 1:28 pm
- before session
- I thought about the logic of the customer choosing a teller
- here's what I know:
  - customer gets in line
  - customer scans the tellers and see if any are available
    - if they are then choose that one
    - this should be in a lock since one thread is scanning at a time
  - if a teller is not available they wait for one to become free
    - they know if a teller is free if teller signals they are free
      - try to acquire isTellerReady[id]
      - will use tryAcquire instead of just acquire, since tryAcquire returns true or false immediately
  - may want to double-check if teller is actually available, since another customer could have picked up the release signal
- for this session I plan on finishing the code
- to test I will start with 1-3 customers and debug

11-06-25 5:51 pm
- after session
- I completed the code and am now debugging it
- When running with one customer and three tellers the data flow seems fine but the program does not automatically stop after the last customer leaves
  - the program should be checking for if all the customer a served and if they are the bank should close
- For next session I will continue to debug and hopefully figure out the issue
- I will also add comments and see if the program works on utd cs server

11-06-25 10:34 pm
- before session
- I think the problem is something in the loop in teller class
  - since the print statement of teller leaving the bank is not reached and that is out of the loop
- for this session I plan on debugging this some more

11-06-25 11:50 pm
- after debugging finally discovered what was wrong
- first, I realized that the customer variable that was keeping track of the amount of customers services wasn't updating
  - this was a logic error, so I rearranged the order in the customer class to have the increment happen before the signal to teller
- after fixing that, there was another error, the program was still not ending
  - turns out that I needed to add logic that signaled the other waiting tellers that the last customer was services
    - at the very end of customer class, I had the last customer signal the other tellers
- next session I will test this with 50 customers
- I will see if this works on the utd cs server
- I will also finish adding comments

11-07-25 6:18 pm
- before session
- Today I will test program with 50 customers to see if the logic still holds
- I will also see if this code works on the utd servers

11-07-25 7:01 pm
- after session
- code logic seems in order for 50 customers
- code works on cs1 machines
- added comments