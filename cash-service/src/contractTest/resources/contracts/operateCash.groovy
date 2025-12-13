package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should operate cash")
    request {
        method POST()
        url("/cash") {}
        headers {
            contentType(applicationJson())
        }
        body(
                id: 3,
                ownerId: 1,
                operation: "PUT",
                email: "test@test.ru",
                amount: 1000
        )
    }
    response {
        status OK()
    }
}
