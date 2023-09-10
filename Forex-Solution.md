# A local proxy for Forex rates Solution Design 

# Functional Requirement Analysis
Given below functional requirements:
* The service returns an exchange rate when provided with 2 supported currencies
* The rate should not be older than **5 minutes**
* The service should support at least **10,000** successful requests per day with 1 API token

And the following drawback of the [One-Frame service](https://hub.docker.com/r/paidyinc/one-frame):
* The One-Frame service supports a maximum of **1000** requests per day for any given authentication token.

It is obvious that it is impossible to make it by simply redirect every request of proxy to One-Frame service directly. 
However, we are allowed to use same response within 5 minutes. So the first thing comes to my mind is **cache**, we can cache the response of 
same currency pair in proxy and return directly without invoking One-Frame service as long as it is within 5 minutes.

In the following document, I will explain the cache solution as final design of this proxy 
# Solution Architecture
## Solution with Cache
![CacheSolution.png](docs%2FCacheSolution.png)
* When proxy starts, there will be a scheduler start to execute every 2.5 minutes (configurable) to query rate for all currencies 
(as One-Frame service can accept multiple currency pair in one request, by this we way can reduce the count of request sent) supported by proxy from One-Frame Service
* * If got valid response, refresh latest rate pair into cache
* * if failed to get response, redo the execution
* When requests from user comes, after basic authentication and parameter validation, we will directly return the rate pair in cache as http response.
* * By right, the cache result should always be not null, but in case failed to get result from cache, we redo the cache refresh task (same with Scheduler does) then return response
* The Cache implemented in this solution is Memcached, but we can use other alternative solution like Redis in production.
### Pros 
1) We send request to One-Frame service every 2.5 minute, then there will be only 24 * (60 minute / 2.5 minutes) = **576** requests will be sent to One-Frame service
2) The cache expire time is 3 minutes (configurable, but should be between [2.5, 5], so we can guarantee the rate pair is no older than 5 minutes
3) As we use cache to serve request, there is **no limit** on total count of requests of each token per day, and we are able to support much higher volume of concurrent requests with very slow latency
### Cons
1) Although in this project, we can get rate of all currency pairs in one request, 
but in real worlds and especially crypto business, it is hard as there will be hundreds of official fiat currency and even thousands of crypto currency,
it is hard to fetch rates of all pairs in one request and also need larger Cache memory size.
2) We need a solid disaster recovery solution for Cache crash case, or it will result in either bad experience to users or spike of request to downstream One-Frame Service 

The solution to the Cons is out of Scope of this solution, I will share my potential solution in appendix.
### Non-Functional Analysis
1. **[Scalability]** As we serve user's request by cache, so it is easier to scale out our service throughput by increasing memory size or using more powerful Cache solution like Redis
2. **[Reliability]** The maintenance of rate and requests from users are decoupled, which means downstream rate service would not impact the stability of proxy; 
and it is very easy to add-on other external rate providers and switch-off the dependency on unavailable providers 


# How to run locally

#### How to run locally

* Pull the docker image with `docker pull paidyinc/one-frame`
* Run the service locally on port 8080 with `docker run -p 8080:8080 paidyinc/one-frame`

#### Usage
__API__

The One-Frame API offers two different APIs, for this exercise please use the `GET /rates` one.

`GET /rates?pair={currency_pair_0}&pair={currency_pair_1}&...pair={currency_pair_n}`

pair: Required query parameter that is the concatenation of two different currency codes, e.g. `USDJPY`. One or more pairs per request are allowed.

token: Header required for authentication. `10dc303535874aeccc86a8251e6992f5` is the only accepted value in the current implementation.

__Example cURL request__
```
$ curl -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:8080/rates?pair=USDJPY'

[{"from":"USD","to":"JPY","bid":0.61,"ask":0.82,"price":0.71,"time_stamp":"2019-01-01T00:00:00.000"}]
```

# Appendix
## Potential Solution to Cons of Cache Solution
### Supports to plenty of currencies and even crypto and earn profit
Firstly, let us think about the classic **shortest-path** problem, there are multiple roads from A to B, we try to figure out shortest one by comparing
> A -> C -> D -> B or A -> E -> F -> B

which means, there is no need to be a direct way between A and B.

Let us go back to this currency pair case, similarly, if we need the best (or just a valid) pair between USD and JPY,
there is **no need to be** a direct pairs for USD->JPY, as long as there are:
> USD -> SGD, SGD -> AUD, AUD -> JPY

or 
> USD -> BNB, BNB -> BTC, BTC -> ETH, ETH -> USDT, USDT -> JPY

in cache result. Then by applying some calculation of the bid, ask and price of each pair, 
we can finally get a rate pair with best price, and make **profits** from it. 


