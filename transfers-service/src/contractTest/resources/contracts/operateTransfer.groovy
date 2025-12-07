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
                accountIdFrom: 3,
                currencyFrom: "RUB",
                accountIdTo: 2,
                currencyTo: "EUR",
                email: "test@test.ru",
                amountFrom: 200
        )
    }
    response {
        status OK()
    }
}