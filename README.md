# os-project2

Overview:
This program simulates a bank environment via multithreading and synchronization in Java.
There are 3 tellers and 50 customers, and each customer visits the bank to either deposit or withdraw money. The bank makes sure:
- Its doors open when all 3 tellers are ready
- Only 2 customers go through the door at a time, enter the bank, wait in line, until a teller is ready to serve them
- Customer goes up to the teller and gives its id
- Teller asks for transaction type and customer answers
- For withdrawals the teller goes to the manager, which only 1 teller is allowed to talk to at a time
- For both withdrawals and deposits the teller will go to the safe, which only 2 tellers are allowed to use at a time
- Teller returns to customer saying transaction is done
- Customer leaves the bank while Teller waits
- Once all custmoers are served the bank closes

Project demonstrates use of Threads, Semaphores, and Mutexes.

Files
- only one file Bank.java, with a Teller and Customer class
- to compile type in javac Bank.java
- to run type java Bank
- no additional parameters
