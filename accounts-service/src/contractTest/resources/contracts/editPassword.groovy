package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should register account")
    request {
        method POST()
        url("/accounts/3/editPassword") {}
        headers {
            contentType(applicationJson())
        }
        body(
                newPassword: "Password1111",
                confirmPassword: "Password1111"
        )
    }
    response {
        status OK()
    }
}
