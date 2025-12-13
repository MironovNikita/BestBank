package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should operate transfer")
    request {
        method POST()
        url("/transfer") {}
        headers {
            contentType(applicationJson())
        }
        body(
                accountIdFrom: 2,
                currencyFrom: "RUB",
                accountIdTo: 3,
                currencyTo: "EUR",
                email: "test@test.ru",
                amountFrom: 1000
        )
    }
    response {
        status OK()
    }
}