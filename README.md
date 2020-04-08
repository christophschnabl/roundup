# 💸 👆 Roundup

## Build

## Run

Make sure the token environment variable is set 

## Implementation

### Assumptions 

Currency is always GBP

Application is stateless so a new savings goal is created each time. no record of which transactions already rounded

all accounts -> into one accounts savings goal 
only first account is taken in consideration 

Testing happy path 

no rest resource 
Notes on how the API is consumed:
I made certain decisions that would not be done in a production grade system to not break the limit:
No Circuit Breakers implementeds