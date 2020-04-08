# 💸 👆 Roundup

The following application runs the round up for the configured user since the first day of the week for all outgoing (non-internal) transactions and transfers this sum into the configured savings goal (which is created also).

 

## Build

Run `mvn package` from within the root directory of the project (`cd roundup/`)

## Run

Make sure the token environment variable is set:
```
STARLING_TOKEN=ey....
echo $STARLING_TOKEN
```

Run the packaged fat-jar as follows (from within the root directory of the project):

`java -jar target/roundup-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Implementation


### Assumptions 

A few assumptions were made to focus on the core feature and not break the time limit, however the following aspects need to be taken into account for a production-ready solution:
* The name of the goal and the target are provided in the main function
* The currency is assumed to always be GBP
* The application is stateless
    * A new savings goal is created for each invocation 
    * No record of already processed transfers is kept
* The transactions of all accounts for a user are transferred into the first accounts savings goal
* The user is inferred from the provided application token  
* Integration and Application Tests only cover the happy path and now corner cases
* No Circuit Breakers
* No Retry Strategy
*   API Errors are just masked into exceptions and propagated to the user 
* No Logging added
    * Adding it by chaining another interceptor could be done very easily
    * Logging in other classes would not be a big deal, either
