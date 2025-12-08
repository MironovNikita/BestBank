package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should login user")
    request {
        method GET()
        url("/accounts/currencies/3")
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                [
                        [id: "1", ownerId: "3", title: "Test", currency: "RUB", balance: "1000.00"] as Map,
                        [id: "2", ownerId: "3", title: "Test", currency: "EUR", balance: "10.00"] as Map
                ]
        )
    }
}
