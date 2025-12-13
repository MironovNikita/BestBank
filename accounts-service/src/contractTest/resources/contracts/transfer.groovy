package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should transfer")
    request {
        method POST()
        url("/accounts/transfer") {}
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
