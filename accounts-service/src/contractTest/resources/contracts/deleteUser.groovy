package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should delete user")
    request {
        method POST()
        url("/users/delete/3") {}
        headers {
            contentType(applicationJson())
        }
        body(
                "test@test.ru"
        )
    }
    response {
        status OK()
    }
}
