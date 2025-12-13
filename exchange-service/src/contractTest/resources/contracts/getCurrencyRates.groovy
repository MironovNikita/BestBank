package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Get currency rates")
    request {
        method GET()
        urlPath("/exchange/rates")
    }
    response {
        status OK()
        headers { contentType(applicationJson()) }
        body([
                [
                        currency: "RUB",
                        buy     : 1.00,
                        sell    : 1.00
                ],
                [
                        currency: "USD",
                        buy     : 5.43,
                        sell    : 6.12
                ],
                [
                        currency: "EUR",
                        buy     : 2.15,
                        sell    : 3.25
                ]
        ])
    }
}
