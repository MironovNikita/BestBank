package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should not register user if not valid params")
    request {
        method POST()
        url("/users/register") {}
        headers {
            contentType(applicationJson())
        }
        body(
                email: "test",
                password: "Pa",
                name: "Test",
                surname: "Test",
                birthdate: "1990-01-01",
                phone: "99"
        )
    }
    response {
        status BAD_REQUEST()
    }
}
