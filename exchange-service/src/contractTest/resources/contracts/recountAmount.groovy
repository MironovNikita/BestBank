package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should recount original account")
    request {
        method POST()
        url("/exchange/recount") {}
        headers {
            contentType(applicationJson())
        }
        body(
                amount: 200,
                originalCurrency: "RUB",
                targetCurrency: "EUR"
        )
    }
    response {
        status OK()
        headers { contentType(applicationJson()) }
        body(
                2
        )
    }
}
