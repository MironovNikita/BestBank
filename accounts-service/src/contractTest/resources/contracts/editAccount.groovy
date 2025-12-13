package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should edit account")
    request {
        method POST()
        url("/accounts/edit") {}
        headers {
            contentType(applicationJson())
        }
        body(
                id: "2",
                newTitle: "New Title",
                currency: "RUB",
                email: "test@test.ru"
        )
    }
    response {
        status OK()
    }
}
