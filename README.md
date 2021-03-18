Prerequisites: Java 11

To build and deploy: 
Run ./run_script.sh from terminal 

To Test:

Once application is deployed, go to localhost:8080 from your browser. 
A swagger page should open up with the 3 APIs. Open each of them as required, fill the payload JSON and click Execute

Check the Response Body and Response headers in the swagger page for response


Assumption Taken
There might be negative transactions that have timestamps later than the positive transactions. We dont want to spend the positive points while keeping the negative points in the system as it will lead to negative balance. As a result, negative balances are moved up above all positive balances in the queue to ensure they are spent (and thus eliminated from the system) with every transaction.
