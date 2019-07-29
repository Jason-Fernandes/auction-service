# auction-service

----------------------------------------------------------------------------------

**About the Web App:**
--

This web app functions as an auto-bid auction system. Bidder information is submitted towards a particular auction, and then the current winning bid for that auction is immediately calculated. The winning bid information consists of the winner's name, the auction id they're winning, and the lowest amount possible they are bidding while maintaining their first place bid. If there is a tie between two or more bidders, the first person that entered their information wins.


The following bidder information is needed for each submission:

    "bidderId"- Unique bidder's ID.
    "startingBid"- The first and lowest bid the buyer is willing to offer for the item
    "maxBid"- This maximum amount the bidder is willing to pay for the item
    "autoIncrement"- A dollar amount which will be added to the bidder's current bid each time the bidder is in a losing position relative to the other bidders. The current bid will never exceed the Max bid. The current bid will only increment by the exact auto‑increment amount.
    "auctionId"- The particular auction ID which the bid will be applied.



The winning bid information consists of the following:

    "bidderId"- The winning bidder's ID.
    "winningPrice"- The lowest amount possible the winner is willing to pay while maintaining a higher price over all previous bid submissions.
    "auctionId"- The particular auction ID which this winning bid applies to.


----------------------------------------------------------------------------------


**Starting Up auction-service Application:**
--

To build the application, you will need to have Maven installed. Once Maven is installed, run the following command:

`mvn clean install`


In the Root Directory, run the following command to start the API:

`java -jar target/auction-service-0.0.1-SNAPSHOT.jar server config.yml`

----------------------------------------------------------------------------------

**Health Check**
--
To see your applications health enter url `http://localhost:8081/healthcheck`

----------------------------------------------------------------------------------

**API DESIGN**
--
There are two endpoints.

GET retrieves the winner and winningPrice of a specified auction by inputting an "auctionId".

Example Request, Query Parameter is "auctionId" which should represent a whole number > 0:
http://localhost:8080/auction?auctionId=1

Example Json Response, "code" is the Http Status Code, "bidderId" is the unique Id originally passed in with a bid.
"auctionId" corresponds to the unique auction this winningBid's information also corresponds to. And "winningPrice" is
the current bid of the winner. The response maintains appropriate dollar sign, commas, and decimals on monetary values.
{
    "code": 200,
    "bidderId": "John",
    "winningPrice": "$1,000.34",
    "auctionId": 1
}


POST processes a Bid against the current winner. When a Bid is submitted, it initially only compared against one other
bid.

Example Json Body Request, The monetary values can be passed with  dollar signs, commas, and decimal places. The
"bidderId" is a uniqueId passed in from the Front End and is associated with a bidder. The "startingBid" is the initial
amount of money a bidder is willing to immediately offer. The "maxBid" is the upper bound a bidder is willing to bid,
but it is not necessarily the highest bid a bidder will make. The "autoIncrement" field represents the increments in
which a bidder is willing to increase their current bid by, so long as their total bid does not exceed "maxBid".
"auctionId" represents the particular auction (and respective winner bid) this bid information will be compete against.

http://localhost:8080/auction
{
    "bidderId": "Alice",
    "startingBid": "$1,000.34",
    "maxBid": "4,000",
    "autoIncrement": "$2.000",
    "auctionId": "1"
}

Example Json Response, "code" is the Http Status Code. "bid" is an object containing all of the previously mentioned
bid information fields. The Response does not contain $dollar signs nor commas on monetary values. The response gives
the same information passed in to provide confirmation for the information passed in and processed. This is a POST
action in the sense that a new object was being introduced into the system by the client, and is potentially being
stored if it is the current winner of the bid. Ultimately, only one bid per auction is being stored so as to limit
memory usage, as opposed to maintaining all bid entries.

{
    "code": 201,
    "bid": {
        "bidderId": "Alice",
        "startingBid": 1000.34,
        "maxBid": 4000,
        "autoIncrement": 2.000,
        "auctionId": 1
    }
}

----------------------------------------------------------------------------------

**Windows Instruction:**
--

**Curl Commands to Access the different endpoints:**
--


GET, replace the following variable: {AUCTION_ID}

`curl -H "Content-Type: application/json" -X GET http://localhost:8080/auction?auctionId={AUCTION_ID}`


GET Example:

`curl -H "Content-Type: application/json" -X GET http://localhost:8080/auction?auctionId=1`




POST, replace the following Json Body arguments: {ID}, {START}, {MAX}, {AUTO}, {AUCTION}

`curl -H "Content-Type: application/json" -X POST http://localhost:8080/auction \
 -d "{\"bidderId\":{ID},\"startingBid\":{START},\"maxBid\":{MAX}, \
 \"autoIncrement\":{AUTO},\"auctionId\":{AUCTION}}`


POST Example:

`curl -H "Content-Type: application/json" -X POST http://localhost:8080/auction \
 -d "{\"bidderId\":\"John\",\"startingBid\":\"$1,000.34\",\"maxBid\":\"4,000\", \
 \"autoIncrement\":\"$.100\",\"auctionId\":\"1\"}`

----------------------------------------------------------------------------------
